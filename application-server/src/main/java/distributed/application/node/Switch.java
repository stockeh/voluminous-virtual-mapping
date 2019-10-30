package distributed.application.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Scanner;


import distributed.application.util.Properties;
import distributed.application.wireformats.EventFactory;
import distributed.application.wireformats.Protocol;
import distributed.application.metadata.ServerMetadata;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.wireformats.Event;

/**
 *
 * @author stock
 *
 */
public class Switch implements Node {

  private static final Logger LOG = Logger.getInstance(Properties.SYSTEM_LOG_LEVEL);

  private static final String EXIT = "exit";

  private static final String HELP = "help";

  private final ServerMetadata metadata;


  /**
   * Default constructor - creates a new server tying the <b>host:port</b>
   * combination for the node as the identifier for itself.
   * 
   * @param host
   * @param port
   */
  private Switch(String host, int port) {
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
      Switch node = new Switch( InetAddress.getLocalHost().getHostName(),
          serverSocket.getLocalPort() );

      LOG.info( "Switch node starting up at: " + new Date() + ", on "
          + node.metadata.getConnection() );

      ( new Thread( new TCPServerThread( node, serverSocket, EventFactory.getInstance()),
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
      case Protocol.REGISTER_REQUEST :
        break;
    }
  }

  /**
   * Display a help message for how to interact with the application.
   * 
   */
  private void displayHelp() {
    StringBuilder sb = new StringBuilder();

    sb.append( "\n\t" ).append( EXIT )
        .append( "\t\t: gracefully leave the network and distribute stored " )
        .append( "files.\n" );

    System.out.println( sb.toString() );
  }

}
