package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;

import java.io.IOException;

public class DiscoverResponse implements Event{

  public int type;
  public int mapSize;
  public int sectorSize;
  public String serverToConnect;

  public DiscoverResponse(int type, int mapSize, int sectorSize, String serverToConnect) {
    this.type = type;
    this.mapSize = mapSize;
    this.sectorSize = sectorSize;
    this.serverToConnect = serverToConnect;
  }

  public DiscoverResponse(byte[] bytes) {
      MessageUnMarshaller.readEvent( getClass(), this, bytes );
  }

  @Override
  public int getType() {
    return type;
  }

  @Override
  public byte[] getBytes() throws IOException {
    return MessageMarshaller.writeEvent( getClass(), this );
  }
  
  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }
}
