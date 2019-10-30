package cs555.system.wireformats;

import java.io.IOException;

/**
 * Public interface that each message will implement.
 * 
 * @author stock
 *
 */
public interface Event {

  /**
   * Specify the type of message being sent. This is the first
   * <code>int</code> from the marshalled byte array.
   * 
   * @return The protocol for the message is returned depending on the
   *         event.
   */
  public int getType();

  /**
   * Translates from in-memory to network-bound byte sequence, i.e.,
   * pack fields into a byte array.
   * 
   * @return Returns an array of bytes of the object.
   * @throws IOException
   */
  public byte[] getBytes() throws IOException;

}
