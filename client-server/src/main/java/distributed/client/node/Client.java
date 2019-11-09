package distributed.client.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import distributed.client.metadata.ClientMetadata;
import distributed.client.util.Properties;
import distributed.client.wireformats.EventFactory;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPSender;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.GenericMessage;
import distributed.common.wireformats.Protocol;

/**
 *
 * @author stock
 *
 */
public class Client implements Node {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private static final String EXIT = "exit";

  private static final String HELP = "help";

  private final ClientMetadata metadata;

  private TCPConnection server;

  /**
   * Default constructor - creates a new server tying the
   * <b>host:port</b> combination for the node as the identifier for
   * itself.
   * 
   * @param host
   * @param port
   * @param args
   */
  private Client(String host, int port, String[] args) {
    String initialSector = "0,0";
    if ( args.length > 0 )
    {
      initialSector = args[ 0 ];
    }

    String initialLocation = "0,0";
    if ( args.length > 1 )
    {
      initialLocation = args[ 1 ];
    }
    this.metadata = new ClientMetadata( host, port );
    this.metadata.setNavigation( initialSector, initialLocation );
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
      Client node = new Client( InetAddress.getLocalHost().getHostName(),
          serverSocket.getLocalPort(), args );

      LOG.info( "Client node starting up at: " + new Date() + ", on "
          + node.metadata.getConnection() );

      ( new Thread(
          new TCPServerThread( node, serverSocket, EventFactory.getInstance() ),
          "Client Thread" ) ).start();

      node.connectToSwitch( args );
      node.interact();
    } catch ( IOException e )
    {
      LOG.error(
          "Unable to successfully start server. Exiting. " + e.toString() );
      System.exit( 1 );
    }
  }

  /**
   * Send connection message to switch to request a server
   * 
   * @param args contains the sector location
   * 
   */
  public void connectToSwitch(String[] args) {
    try
    {
      TCPConnection switchConnection = new TCPConnection( this,
          new Socket( Properties.SWITCH_HOST, Properties.SWITCH_PORT ),
          EventFactory.getInstance() );

      switchConnection.startReceiver();

      TCPSender sender = switchConnection.getTCPSender();

      sender.sendData( new GenericMessage( Protocol.DISCOVER_REQUEST,
          metadata.getNavigator().getInitialSector() ).getBytes() );

    } catch ( IOException e )
    {
      LOG.error( "Unable to connect to the Switch. " + e.toString() );
      e.printStackTrace();
    }
    return;
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

        default :
          LOG.error(
              "Unable to process. Please enter a valid command! Input 'help'"
                  + " for options." );
          break;
      }
    }
    LOG.info(
        metadata.getConnection() + " has unregistered and is terminating." );
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
      case Protocol.DISCOVER_RESPONSE :
        connectToServer( event, connection );
        break;

      case Protocol.REGISTER_CLIENT_RESPONSE :
        // TODO: Start moving the client about the environment
        LOG.info( "Client successfully connected to the server!" );
        break;
    }
  }

  /**
   * Initiate a connection with the server and close the connection to
   * the connection.
   * 
   * @param event
   * @param connection
   */
  private void connectToServer(Event event, TCPConnection connection) {
    String[] connectionIdentifier =
        ( ( GenericMessage ) event ).getMessage().split( ":" );
    try
    {
      server = new TCPConnection( this,
          new Socket( connectionIdentifier[ 0 ],
              Integer.parseInt( connectionIdentifier[ 1 ] ) ),
          EventFactory.getInstance() );

      server.startReceiver();

      server.getTCPSender()
          .sendData( new GenericMessage( Protocol.REGISTER_CLIENT_REQUEST,
              metadata.getNavigator().getInitialSector() ).getBytes() );

    } catch ( IOException e )
    {
      LOG.error( "Unable to connect client to Server " + e.toString() );
      e.printStackTrace();
      System.exit( 1 );
    }
    connection.close();
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
