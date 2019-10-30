package cs555.system.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import cs555.system.util.Logger;

/**
 * Singleton class in charge of creating objects, i.e., messaging
 * types, from reading the first byte of a message.
 * 
 * @author stock
 *
 */
public class EventFactory {

  private static final Logger LOG = Logger.getInstance();

  private static final EventFactory instance = new EventFactory();

  /**
   * Default constructor - Exists only to defeat instantiation.
   */
  private EventFactory() {}

  /**
   * Single instance ensures that singleton instances are created only
   * when needed.
   * 
   * @return Returns the instance for the class
   */
  public static EventFactory getInstance() {
    return instance;
  }

  /**
   * Override the clone method to ensure the "unique instance"
   * requirement of this class.
   * 
   */
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  /**
   * Create a new event, i.e., wireformat object from the marshalled
   * bytes of said object.
   * 
   * @param message
   * @return the event object from the <code>byte[]</code>.
   * @throws IOException
   */
  public Event createEvent(byte[] marshalledBytes) throws IOException {
    switch ( ByteBuffer.wrap( marshalledBytes ).getInt() )
    {
      case Protocol.REGISTER_REQUEST :
        return new GenericMessage( marshalledBytes );

      default :
        LOG.error( "Event could not be created. "
            + ByteBuffer.wrap( marshalledBytes ).getInt() );
        return null;
    }
  }
}