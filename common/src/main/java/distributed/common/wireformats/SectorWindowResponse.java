package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;

import java.io.IOException;

public class SectorWindowResponse implements Event{
  public int type;
  public byte[][] sectorWindow;
  public int numSectors;

  public SectorWindowResponse(int type, byte[][] sectorWindow, int numSectors) {
    this.type = type;
    this.sectorWindow = sectorWindow;
    this.numSectors = numSectors;
  }

  public SectorWindowResponse(byte[] marshalledBytes) throws IOException {
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
}
