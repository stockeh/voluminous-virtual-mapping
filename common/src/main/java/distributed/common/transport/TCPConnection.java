package distributed.common.transport;


import java.io.IOException;
import java.net.Socket;
import distributed.common.node.Node;
import distributed.common.util.Logger;
import distributed.common.wireformats.Factory;

/**
 * This class is used to establish a connection by starting a new
 * TCPReceiverThread and associating the TCPSender with the socket.
 * 
 * @author stock
 *
 */
public class TCPConnection {

  private static final Logger LOG = Logger.getInstance( "debug" );

  private Socket socket;

  private TCPSender sender;

  private TCPReceiverThread receiver;


  /**
   * Default constructor - create a new TCPConnection given a Node,
   * i.e., chunk server or discovery, and the socket for the connection.
   * 
   * @param node
   * @param socket
   * @param factory
   * @throws IOException
   */
  public TCPConnection(Node node, Socket socket, Factory factory)
      throws IOException {
    this.socket = socket;
    this.sender = new TCPSender( this.socket );
    this.receiver = new TCPReceiverThread( node, this.socket, this, factory );
  }

  /**
   * Get the Socket for the connection to verify INet information
   * 
   * @return the socket for the connection.
   */
  public Socket getSocket() {
    return this.socket;
  }

  public String getEndHost() { return this.socket.getInetAddress().getCanonicalHostName(); }
  /**
   * Get the TCPSender so the server or server can send a message over
   * the socket
   * 
   * @return the TCPSender instance that was instantiated during the
   *         {@link ()} method of the new thread.
   */
  public TCPSender getTCPSender() {
    return this.sender;
  }

  /**
   * Allow the TCPConnection to start receiving messages.
   * 
   */
  public void startReceiver() {
    new Thread( this.receiver ).start();
  }

  /**
   * Close the socket sender and receiver.
   * 
   */
  public void close() {
    try
    {
      this.sender.dout.close();
      this.receiver.din.close();
      this.socket.close();
    } catch ( IOException e )
    {
      LOG.error( "Unable to close the connection with node. " + e.toString() );
      e.printStackTrace();
    }
  }
}
