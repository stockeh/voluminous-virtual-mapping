package cs555.system.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class used to send data, via <code>byte[]</code> to the receiver.
 * 
 * Running as a thread, the TCPConnection holds an instance to the
 * sender for new messages. This makes use of a linked blocking queue
 * to buffer the rate at which messages are being sent.
 * 
 * @author stock
 *
 */
public class TCPSender {

  protected DataOutputStream dout;

  /**
   * Default constructor - Initialize the TCPSender data output
   * stream information from the <code>socket</code>.
   * 
   * @param socket
   * @throws IOException
   */
  public TCPSender(Socket socket) throws IOException {
    this.dout = new DataOutputStream( socket.getOutputStream() );
  }

  /**
   * Send the data through the socket connection using the data output
   * stream. Write the length first, and then the actual data - that way
   * the receiver knows when to stop reading.
   * 
   * @throws IOException 
   */
  public synchronized void sendData(final byte[] data) throws IOException {
    int len = data.length;
    dout.writeInt( len );
    dout.write( data, 0, len );
    dout.flush();
  }
}
