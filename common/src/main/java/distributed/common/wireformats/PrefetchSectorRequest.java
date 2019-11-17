package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.util.Sector;

import java.io.IOException;
import java.util.Set;

public class PrefetchSectorRequest implements Event{
  public int type;
  public Set<Sector> sectors;

  public PrefetchSectorRequest(int type, Set<Sector> sectors) {
    this.type = type;
    this.sectors = sectors;
  }

  public PrefetchSectorRequest(byte[] marshalledBytes) throws IOException {
    MessageUnMarshaller.readEvent( getClass(), this, marshalledBytes );
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
