package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.util.Sector;
import java.io.IOException;
import java.util.Set;

/**
 * 
 * @author stock
 *
 */
public class SectorWindowRequest implements Event {

  public int type;

  public long initialTimestamp;

  public Set<Sector> sectors;

  public Sector currentSector;

  public int windowSize;

  public int[] position;

  public int numRequestedSectors;

  public String host;

  public int port;

  public boolean loadSector;

  public boolean updatePrimaryServer;

  /**
   * Constructor without assigned message
   * 
   */
  public SectorWindowRequest(int type, long initialTimestamp,
      Set<Sector> sectors, Sector currentSector, int windowSize,
      int[] position, int numRequestedSectors, String host, int port, boolean loadSector, boolean updatePrimaryServer) {
    this.type = type;
    this.initialTimestamp = initialTimestamp;
    this.sectors = sectors;
    this.currentSector = currentSector;
    this.windowSize = windowSize;
    this.position = position;
    this.numRequestedSectors = numRequestedSectors;
    this.host = host;
    this.port = port;
    this.loadSector = loadSector;
    this.updatePrimaryServer = updatePrimaryServer;
  }


  public SectorWindowRequest(int type, long initialTimestamp,
                             Set<Sector> sectors, Sector currentSector, int windowSize,
                             int[] position, int numRequestedSectors, int port) {
    this(type, initialTimestamp, sectors, currentSector, windowSize, position, numRequestedSectors, "", port, false, false);
  }

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public SectorWindowRequest(byte[] marshalledBytes) throws IOException {
    MessageUnMarshaller.readEvent( getClass(), this, marshalledBytes );

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return type;
  }

  public long getInitialTimestamp() {
    return initialTimestamp;
  }

  public Set<Sector> getSectors() {
    return sectors;
  }

  public int getWindowSize() {
    return windowSize;
  }

  public int[] getPosition() {
    return position;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getBytes() throws IOException {
    return MessageMarshaller.writeEvent( getClass(), this );
  }

  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }

}
