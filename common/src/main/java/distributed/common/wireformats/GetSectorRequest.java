package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.util.Sector;

import java.io.IOException;

public class GetSectorRequest implements Event{

  public int type;
  public Sector sector;

  public GetSectorRequest(int type, Sector sector) {
    this.type = type;
    this.sector = sector;
  }

  public GetSectorRequest(byte[] bytes) {
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
}
