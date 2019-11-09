package distributed.common.wireformats;

import java.io.IOException;
import java.util.Set;

public class SectorWindowResponse implements Event{
  public int type;
  public byte[][] sectorWindow;
  public int numSectors;
  Set<String> sectorIDs;

  public SectorWindowResponse(int type, byte[][] sectorWindow, int numSectors, Set<String> sectorIDs) {
    this.type = type;
    this.sectorWindow = sectorWindow;
    this.numSectors = numSectors;
    this.sectorIDs = sectorIDs;
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
