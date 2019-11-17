package distributed.application.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import distributed.application.util.Properties;
import distributed.common.util.Logger;
import distributed.common.wireformats.*;

/**
 * Singleton class in charge of creating objects, i.e., messaging
 * types, from reading the first byte of a message.
 * 
 * @author stock
 *
 */
public class EventFactory implements Factory {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

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
   * @return the event object from the <code>byte[]</code>.
   * @throws IOException
   */
  public Event createEvent(byte[] marshalledBytes) throws IOException {
    switch ( ByteBuffer.wrap( marshalledBytes ).getInt() )
    {
      case Protocol.LOAD_SECTOR:
      case Protocol.GET_SECTOR_REQUEST :
        return new GenericSectorMessage( marshalledBytes );

      case Protocol.SECTOR_WINDOW_REQUEST :
        return new SectorWindowRequest( marshalledBytes );

      case Protocol.SECTOR_LOADED:
      case Protocol.REGISTER_SERVER_REQUEST :
      case Protocol.REGISTER_SERVER_RESPONSE :
      case Protocol.SERVER_INITIALIZED :
        return new GenericMessage( marshalledBytes );

      case Protocol.CLIENT_DISCOVER_REQUEST :
        return new ClientDiscoverRequest( marshalledBytes );

      case Protocol.APPLICATION_HEATBEAT :
        return new ApplicationHeartbeat( marshalledBytes );

      case Protocol.PREFETCH_SECTORS:
        return new PrefetchSectorRequest( marshalledBytes );

      default :
        LOG.error( "Event could not be created. "
            + ByteBuffer.wrap( marshalledBytes ).getInt() );
        return null;
    }
  }
}
