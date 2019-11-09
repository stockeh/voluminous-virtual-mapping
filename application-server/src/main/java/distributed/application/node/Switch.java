package distributed.application.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import distributed.application.metadata.ServerInformation;
import distributed.application.metadata.SwitchMetadata;
import distributed.application.util.Constants;
import distributed.application.util.Properties;
import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.application.wireformats.EventFactory;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPSender;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.GenericMessage;
import distributed.common.wireformats.Protocol;

/**
 *
 * @author stock
 *
 */
public class Switch implements Node {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private static final String LIST_SERVERS = "list-servers";

  private static final String EXIT = "exit";

  private static final String HELP = "help";

  private final SwitchMetadata metadata;


  /**
   * Default constructor - creates a new server tying the
   * <b>host:port</b> combination for the node as the identifier for
   * itself.
   * 
   * @param host
   * @param port
   */
  private Switch(String host, int port) {
    this.metadata = new SwitchMetadata( host, port );
  }

  /**
   * Start listening for incoming connections and establish connection
   * into the server network.
   *
   * @param args
   */
  public static void main(String[] args) {
    try ( ServerSocket serverSocket =
        new ServerSocket( Integer.valueOf( Properties.SWITCH_PORT ) ) )
    {
      Switch node = new Switch( InetAddress.getLocalHost().getHostName(),
          serverSocket.getLocalPort() );

      LOG.info( "Switch node starting up at: " + new Date() + ", on "
          + node.metadata.getIdentifier() );

      ( new Thread(
          new TCPServerThread( node, serverSocket, EventFactory.getInstance() ),
          "Server Thread" ) ).start();

      node.interact();
    } catch ( IOException e )
    {
      LOG.error(
          "Unable to successfully start server. Exiting. " + e.toString() );
      System.exit( 1 );
    }
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

        case LIST_SERVERS :
          displayServerConnections();
          break;

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
        metadata.getIdentifier() + " has unregistered and is terminating." );
    System.exit( 0 );
  }

  /**
   * Display all of the connected servers.
   * 
   */
  private synchronized void displayServerConnections() {
    Map<String, ServerInformation> serverConnections =
        metadata.getServerConnections();
    if ( serverConnections.size() == 0 )
    {
      LOG.error(
          "There are no connections to identify. Initialize a new server." );
    } else
    {
      LOG.info(
          "\nThere are " + serverConnections.size() + " total servers:\n" );
      serverConnections.forEach( (k, v) -> System.out.println( "\t>\t" + k ) );
      System.out.println();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEvent(Event event, TCPConnection connection) {
    LOG.debug( event.toString() );
    switch ( event.getType() )
    {
      case Protocol.REGISTER_SERVER_REQUEST :
        register( event, connection );
        break;

      case Protocol.DISCOVER_REQUEST :
        clientConnectionHandler( event, connection );
        break;

      case Protocol.APPLICATION_HEATBEAT :
        metadata.processApplicationHeatbeat( ( ApplicationHeartbeat ) event );
        break;
    }
  }

  /**
   * Handle the connection message from the client.
   * 
   * @param connection to the client
   * @param event
   * 
   */
  private void clientConnectionHandler(Event event, TCPConnection connection) {
    String sectorIdentifier = ( ( GenericMessage ) event ).getMessage();
    int row = Integer.parseInt(sectorIdentifier.substring(0,sectorIdentifier.indexOf(',')));
    int col = Integer.parseInt(sectorIdentifier.substring(sectorIdentifier.indexOf(',')));
    Sector sector = new Sector(row, col);
    try {
	    String serverToConnect = metadata.getServer( sector );
	    try
	    {
	      connection.getTCPSender().sendData(
	          new GenericMessage( Protocol.DISCOVER_RESPONSE, serverToConnect )
	              .getBytes() );
	    } catch ( IOException e )
	    {
	      LOG.error( "Unable to connect respond to the Client. " + e.toString() );
	      e.printStackTrace();
	    }
	    LOG.info( "The Client \'"
	        + connection.getSocket().getInetAddress().getCanonicalHostName()
	        + "\' is directed to connect to " + serverToConnect );
    } catch (Exception e) {
    	LOG.error( "Unable to select application server. " + e.toString() );
	      e.printStackTrace();
	}
  }

  /**
   * Register a new server with the switch
   * 
   * @param event
   * @param connection
   */
  private synchronized void register(Event event, TCPConnection connection) {
    GenericMessage request = ( GenericMessage ) event;
    String identifier = request.getMessage();
    metadata.addServerConnection( identifier, connection );

    GenericMessage response =
        new GenericMessage( Protocol.REGISTER_SERVER_RESPONSE,
            Boolean.toString( Constants.SUCCESS ) );
    try
    {
      connection.getTCPSender().sendData( response.getBytes() );
      LOG.info(
          ( new StringBuilder() ).append( "The server [ " ).append( identifier )
              .append( " ] successfully connected!" ).toString() );
    } catch ( IOException e )
    {
      LOG.error( "Unable to send response message to server. " + e.toString() );
      e.printStackTrace();
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

    sb.append( "\n\t" ).append( LIST_SERVERS )
        .append( "\t: display the identifiers of all connected servers.\n" );

    System.out.println( sb.toString() );
  }

}
