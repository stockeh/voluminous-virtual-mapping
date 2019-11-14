package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.util.Sector;

import java.io.IOException;

public class GenericSectorMessage implements Event{

  public int type;
  public Sector sector;

  public GenericSectorMessage(int type, Sector sector) {
    this.type = type;
    this.sector = sector;
  }

  public GenericSectorMessage(byte[] bytes) {
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
