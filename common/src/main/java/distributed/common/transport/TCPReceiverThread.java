package distributed.common.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import distributed.common.node.Node;
import distributed.common.util.Logger;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.Factory;

/**
 * The TCP Receiving Thread to acknowledge new wireformat messages
 * received on the specified connection.
 * 
 * The thread is blocked waiting to read an integer (the protocol for
 * each message). This ensures the thread is not running unless there
 * is something to be read.
 * 
 * @author stock
 *
 */
public class TCPReceiverThread implements Runnable {

  private static final Logger LOG = Logger.getInstance( "debug" );

  private Socket socket;

  protected DataInputStream din;

  private Node node;

  private TCPConnection connection;

  private Factory factory;

  /**
   * Default constructor - Initialize the TCPReceiverThread with the
   * socket and data input stream information
   * 
   * @param node
   * @param socket
   * @param connection
   * @param factory
   * @throws IOException
   */
  public TCPReceiverThread(Node node, Socket socket, TCPConnection connection,
      Factory factory) throws IOException {
    this.node = node;
    this.socket = socket;
    this.connection = connection;
    this.din = new DataInputStream( socket.getInputStream() );
    this.factory = factory;
  }

  /**
   * Start running the thread to read from the data input stream. Create
   * an event from the data and handle it appropriately.
   * 
   * {@inheritDoc}
   */
  @Override
  public void run() {
    while ( socket != null )
    {
      try
      {
        int len = din.readInt();

        byte[] data = new byte[ len ];
        din.readFully( data, 0, len );

        Event event = factory.createEvent( data );
        node.onEvent( event, connection );

      } catch ( IOException e )
      {
        LOG.debug( "Closing connection... " + e );
        break;
      }
    }
  }
}
