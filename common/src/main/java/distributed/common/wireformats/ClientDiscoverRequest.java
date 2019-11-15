package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.util.Sector;

import java.io.IOException;

public class ClientDiscoverRequest implements Event{

  public int type;
  public Sector sector;
  public String clientIdentifier;

  public ClientDiscoverRequest(int type, Sector sector, String clientIdentifier) {
    this.type = type;
    this.sector = sector;
    this.clientIdentifier = clientIdentifier;
  }

  public ClientDiscoverRequest(byte[] bytes) {
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
