package distributed.application.metadata;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import distributed.application.util.Properties;
import distributed.common.util.Logger;
import distributed.common.util.Sector;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ServerMetadata {

  private static final Logger LOG =
          Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private final String identifier;

  // sector identifier , sector information
  private ConcurrentHashMap<Sector, SectorInformation> sectors =
      new ConcurrentHashMap<>();

  /**
   * Default Constructor -
   * 
   */
  public ServerMetadata(String host, int port) {
    this.identifier = host + ":" + port;
    this.sectors = new ConcurrentHashMap<>();
  }

  public SectorInformation getSector(Sector sectorID) {
    return sectors.get( sectorID );
  }

  public void addSector(Sector sectorID, byte[][] bytes) {
    sectors.put( sectorID, new SectorInformation(bytes) );
  }

  public boolean containsSector(Sector sectorID) {
    return sectors.containsKey( sectorID );
  }

  public Set<Sector> getMatchingSectors(Set<Sector> requested) {
    Set<Sector> copy = new HashSet<>( requested );
    copy.retainAll( getSectorIdentifiers() );
    return copy;
  }

  public Set<Sector> getNonMatchingSectors(Set<Sector> requested) {
    Set<Sector> copy = new HashSet<>( requested );
    copy.removeAll( getSectorIdentifiers() );
    return copy;
  }

  public byte[][] getWindow(Sector sectorID, Sector currentSector,
      int row, int col, int windowSize) {
    int r = row;
    int c = col;
    int rowDiff = sectorID.getX() - currentSector.getX(); //
    int colDiff = sectorID.getY() - currentSector.getY(); //2-0
    byte[][] sector = sectors.get(sectorID).getSector();
    if(rowDiff > 0) {
      row = 0 - (Properties.SECTOR_BOUNDARY_SIZE-row);
    }else if(rowDiff < 0){
      row = Properties.SECTOR_BOUNDARY_SIZE+row;
    }

    if(colDiff > 0) {
        col = 0 - (Properties.SECTOR_BOUNDARY_SIZE-col);
    }else if(colDiff < 0) {
      col = Properties.SECTOR_BOUNDARY_SIZE+col;

    }


    int rowStart = Math.max(0, row - windowSize);
    int rowEnd = Math.min(row + windowSize, Properties.SECTOR_BOUNDARY_SIZE - 1);
    int colStart = Math.max(0, col - windowSize);
    int colEnd = Math.min(col + windowSize, Properties.SECTOR_BOUNDARY_SIZE - 1);
    int width = rowEnd - rowStart;
    int height = colEnd - colStart;
    if (rowDiff == 0) {
      height++;
    } else {
      rowEnd--;
    }
    if (colDiff == 0) {
      width++;
    }
    byte[][] window;
    try {
      window = new byte[width][height];
    }catch(NegativeArraySizeException nase) {
      LOG.info("r,c" + r + "," + c + " Row: " + row + " Col: " + col + " current: " + currentSector + " otherSector: " + sectorID);
      throw nase;
    }

    for (int i = rowStart, j = 0; i <= rowEnd; i++, j++) {
      System.arraycopy(sector[i], 0, window[j], 0, height);
    }

    return window;
  }

  /**
   * 
   * @return
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * 
   * @return
   */
  public Set<Sector> getSectorIdentifiers() {
    return sectors.keySet();
  }

}
