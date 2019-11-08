package distributed.application.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import distributed.application.heartbeat.ServerHeartbeatManager;
import distributed.application.io.DFS;
import distributed.application.metadata.ServerMetadata;
import distributed.application.util.Constants;
import distributed.application.util.Properties;
import distributed.application.wireformats.EventFactory;
import distributed.application.wireformats.GenericMessage;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.Protocol;

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

  private void loadFile(String filename) {
    try {
      DFS.readFile(filename);
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

        case LOAD :
          loadFile(input[1]);
          break;
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
    }
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
