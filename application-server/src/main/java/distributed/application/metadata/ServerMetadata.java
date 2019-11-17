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
    int xDiff = sectorID.getX() - currentSector.getX();
    int yDiff = sectorID.getY() - currentSector.getY();
    byte[][] sector = sectors.get(sectorID).getSector();
    if ((xDiff > 0 && currentSector.getX() == 0) || (xDiff < 0 && (currentSector.getX() != Properties.SECTOR_MAP_SIZE - 1 || sectorID.x != 0))) {
      row = sector.length - 1 + row;
    } else if ((xDiff != 0)) {
      row = 0 - row;
    }

    if ((yDiff < 0 && (currentSector.getY() != Properties.SECTOR_MAP_SIZE - 1 || sectorID.y != 0)) || (yDiff > 0 && currentSector.getY() == 0)) {
      col = sector.length - 1 + col;
    } else if ((yDiff != 0)) {
      col = 0 - col;

    }

    int rowStart = Math.max(0, row - windowSize);
    int rowEnd = Math.min(row + windowSize, sector.length - 1);
    int colStart = Math.max(0, col - windowSize);
    int colEnd = Math.min(col + windowSize, sector[0].length - 1);
    int width = rowEnd - rowStart;
    int height = colEnd - colStart;
    if (xDiff == 0) {
      width++;
    } else {
      rowEnd--;
    }
    if (yDiff == 0) {
      height++;
    }

    byte[][] window = new byte[width][height];

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
