package distributed.common.wireformats;

import java.io.IOException;
import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.util.Sector;

public class SectorWindowResponse implements Event {

  public int type;

  public byte[][] sectorWindow;

  public int numSectors;

  public long initialTimestamp;

  public Sector sectorID;

  public int[] position;

  public SectorWindowResponse(int type, byte[][] sectorWindow, int numSectors,
      long initialTimestamp, Sector sectorID, int[] position) {
    this.type = type;
    this.sectorWindow = sectorWindow;
    this.numSectors = numSectors;
    this.initialTimestamp = initialTimestamp;
    this.sectorID = sectorID;
    this.position = position;
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

  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }
}
