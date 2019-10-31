package distributed.common.wireformats;

import java.io.IOException;

/**
 * Interface for the EventFactory to implement
 * 
 * @author stock
 *
 */
public interface Factory {

  Event createEvent(byte[] marshalledBytes) throws IOException;

}
