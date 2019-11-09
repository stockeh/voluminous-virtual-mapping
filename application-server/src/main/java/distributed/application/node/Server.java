package distributed.application.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import distributed.application.heartbeat.ServerHeartbeatManager;
import distributed.application.io.DFS;
import distributed.application.metadata.ServerMetadata;
import distributed.application.util.Constants;
import distributed.application.util.Functions;
import distributed.application.util.Properties;
import distributed.application.wireformats.EventFactory;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.*;

/**
 *
 * @author stock
 *
 */
public class Server implements Node {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private static final String EXIT = "exit";

  private static final String HELP = "help";

  private static final String LOAD = "load";

  private final ServerMetadata metadata;


  /**
   * Default constructor - creates a new server tying the
   * <b>host:port</b> combination for the node as the identifier for
   * itself.
   * 
   * @param host
   * @param port
   */
  private Server(String host, int port) {
    this.metadata = new ServerMetadata( host, port );
  }

  /**
   * Start listening for incoming connections and establish connection
   * into the server network.
   *
   * @param args
   */
  public static void main(String[] args) {
    try ( ServerSocket serverSocket = new ServerSocket( 0 ) )
    {
      Server node = new Server( InetAddress.getLocalHost().getHostName(),
          serverSocket.getLocalPort() );

      LOG.info( "Server node starting up at: " + new Date() + ", on "
          + node.metadata.getIdentifier() );

      ( new Thread(
          new TCPServerThread( node, serverSocket, EventFactory.getInstance() ),
          "Server Thread" ) ).start();

      node.discoverSwitchConnection();

      node.interact();
    } catch ( IOException e )
    {
      LOG.error(
          "Unable to successfully start server. Exiting. " + e.toString() );
      System.exit( 1 );
    }
  }

  private void loadFile(Sector sectorID) {
    if(metadata.containsSector(sectorID)) {
      LOG.info("Server already contains sector, not reloading");
      return;
    }
    try {
      String filename = Properties.HDFS_FILE_LOCATION;
      byte[][] bytes = Functions.reshape(DFS.readFile(filename));
      metadata.addSector(sectorID, bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Connect the server to the switch
   * 
   * @throws IOException if unable to connect to the switch, and thus,
   *         the network
   */
  private void discoverSwitchConnection() throws IOException {
    Socket socketToSwitch =
        new Socket( Properties.SWITCH_HOST, Properties.SWITCH_PORT );
    TCPConnection connection =
        new TCPConnection( this, socketToSwitch, EventFactory.getInstance() );
    connection.startReceiver();

    GenericMessage request = new GenericMessage(
        Protocol.REGISTER_SERVER_REQUEST, metadata.getIdentifier() );
    connection.getTCPSender().sendData( request.getBytes() );

    ServerHeartbeatManager serverHeartbeatManager =
        new ServerHeartbeatManager( connection, metadata );
    Timer timer = new Timer();
    // 5 seconds intervals in milliseconds
    timer.schedule( serverHeartbeatManager, 1000, 5000 );
  }

  /**
   * Allow support for commands to be specified while the processes are
   * running.
   * 
   */
  private void interact() {
    System.out.println(
        "\nInput a command to interact with processes. Input 'help' for a "
            + "list of commands.\n" );
    boolean running = true;
    while ( running )
    {
      @SuppressWarnings( "resource" )
      Scanner scan = new Scanner( System.in );
      String[] input = scan.nextLine().toLowerCase().split( "\\s+" );
      switch ( input[ 0 ] )
      {

        case EXIT :
          running = false;
          break;

        case HELP :
          displayHelp();
          break;

//        case LOAD :
//          loadFile(input[1]);
//          break;
        default :
          LOG.error(
              "Unable to process. Please enter a valid command! Input 'help'"
                  + " for options." );
          break;
      }
    }
    LOG.info(
        metadata.getIdentifier() + " has unregistered and is terminating." );
    System.exit( 0 );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEvent(Event event, TCPConnection connection) {
    LOG.debug( event.toString() );
    switch ( event.getType() )
    {
      case Protocol.REGISTER_SERVER_RESPONSE :
        registerServerResponseHandler( event, connection );
        break;
      case Protocol.GET_SECTOR_REQUEST :
    	  getSectorRequestHandler(event, connection);
    	  break;

      case Protocol.REGISTER_CLIENT_REQUEST :
        handleIncomingClient( event, connection );
        break;
      case Protocol.SECTOR_WINDOW_REQUEST :
        handleSectorWindowRequest(event, connection);
        break;
    }
  }

  private void handleSectorWindowRequest(Event event, TCPConnection connection) {
    Set<Sector> matchingSectors = metadata.getMatchingSectors(new HashSet<>());
    byte[][] window = metadata.getWindow(matchingSectors, new Sector(1,1),0, 0, 10);
    try {
      connection.getTCPSender().sendData(new SectorWindowResponse(Protocol.SECTOR_WINDOW_RESPONSE, window, 2).getBytes());
    } catch (IOException e) {
      e.printStackTrace();
//    }
  }

  /**
   * 
   * 
   * @param event
   * @param connection
   */
  private void handleIncomingClient(Event event, TCPConnection connection) {

    GenericMessage request = ( GenericMessage ) event;

    // TODO: maintain connection

    LOG.info( new StringBuilder().append( "Client \'" )
        .append(
            connection.getSocket().getInetAddress().getCanonicalHostName() )
        .append( "\' connected to server at sector " )
        .append( request.getMessage() ).toString() );

    GenericMessage response =
        new GenericMessage( Protocol.REGISTER_CLIENT_RESPONSE,
            Boolean.toString( Constants.SUCCESS ) );
    try
    {
      connection.getTCPSender().sendData( response.getBytes() );
    } catch ( IOException e )
    {
      LOG.error( "Unable to send response message to server. " + e.toString() );
      e.printStackTrace();
    }
  }

  private void getSectorRequestHandler(Event event, TCPConnection connection) {
	// TODO Auto-generated method stub
	  GetSectorRequest message = (GetSectorRequest) event;
	  loadFile(message.sector);
  }

/**
   * Display the response status from the switch.
   * 
   * @param event
   * @param connection
   */
  private void registerServerResponseHandler(Event event,
      TCPConnection connection) {
    if ( Boolean.parseBoolean(
        ( ( GenericMessage ) event ).getMessage() ) == Constants.SUCCESS )
    {
      LOG.info( "The server successfuly connected with the switch!" );
    } else
    {
      LOG.error(
          "The server was NOT able to connect with the switch successfully. Exiting!" );
      connection.close();
      System.exit( 1 );
    }
  }

  /**
   * Display a help message for how to interact with the application.
   * 
   */
  private void displayHelp() {
    StringBuilder sb = new StringBuilder();

    sb.append( "\n\t" ).append( EXIT )
        .append( "\t\t: gracefully leave the network.\n" );

    System.out.println( sb.toString() );
  }

}
