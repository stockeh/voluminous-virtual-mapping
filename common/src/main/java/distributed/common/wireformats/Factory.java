package distributed.common.wireformats;

import java.io.IOException;

public interface Factory {
	Event createEvent(byte[] marshalledBytes) throws IOException;
}
