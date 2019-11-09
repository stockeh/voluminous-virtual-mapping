package distributed.application.metadata;

import distributed.application.util.Properties;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Sector;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ServerMetadata {

  private final String identifier;

  // sector identifier , sector information
  private ConcurrentHashMap<Sector, SectorInformation> sectors = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Sector, List<TCPConnection>> waitingClients;
//  private ConcurrentHashMap<String, SectorInformation> sectors;

  public void addSector(Sector sectorID, byte[][] sector) {
    sectors.put(sectorID, new SectorInformation(sector));
  }

  public boolean containsSector(Sector sectorID) {
    return sectors.containsKey(sectorID);
  }

  public final List<TCPConnection> getWaitingClients(Sector sectorID) {
    return waitingClients.get(sectorID);
  }

  public final void addWaitingClient(Sector sectorID, TCPConnection client) {
    waitingClients.putIfAbsent(sectorID, Collections.synchronizedList(new ArrayList<>()));
    waitingClients.get(sectorID).add(client);
  }

  public final void clearWaitingClients(Sector sectorID) {
    sectors.remove(sectorID);
  }

  public Set<Sector> getMatchingSectors(Set<Sector> requested) {
    Set<Sector> copy = new HashSet<>(requested);
    copy.retainAll(getSectorIdentifiers());
    return copy;
  }
  
  /**
   * Default Constructor -
   * 
   */
  public ServerMetadata(String host, int port) {
    this.identifier = host + ":" + port;
    this.sectors = new ConcurrentHashMap<>();
  }

  public byte[][] getWindow(Set<Sector> sectorIDs, Sector currentSector, int row, int col, int windowSize) {
    int width = 0;
    int height = 0;
    for (Sector id : sectorIDs) {
      byte[][] sector = sectors.get(id).getSector();
      int rowStart = Math.max(0, row - windowSize);
      int rowEnd = Math.min(row + windowSize + 1, sector.length);
      int colStart = Math.max(0, col - windowSize);
      int colEnd = Math.min(col + windowSize + 1, sector[0].length);
      width += rowEnd - rowStart;
      height += colEnd - colStart;
    }

    byte[][] window = new byte[width][height];

    for (Sector id : sectorIDs) {
      byte[][] sector = sectors.get(id).getSector();
      int r = row;
      int c = col;
      int xDiff = id.getX() - currentSector.getX();
      int yDiff = id.getX() - currentSector.getX();

      if (xDiff < 0) {
        r = sector.length + row;
        if (Math.abs(xDiff) > 1) r += sector.length;
      } else if (xDiff > 0) {
        r = 0 - row;
        if (Math.abs(xDiff) > 1) r -= sector.length;
      }

      if (yDiff < 0) {
        c = sector.length + col;
        if (Math.abs(yDiff) > 1) c += sector.length;
      } else if (yDiff > 0) {
        c = 0 - col;
        if (Math.abs(yDiff) > 1) c -= sector.length;

      }
      window = getWindow(sector, r, c, windowSize, window);
    }
    return window;

  }

  private byte[][] getWindow(byte[][] sector, int row, int col, int windowSize, byte[][] window) {

    if(sector == null) {
      return window;
    }else {
      int rowStart = Math.max(0, row-windowSize);
      int rowEnd = Math.min(row+windowSize+1, sector.length);
      int colStart = Math.max(0, col-windowSize);
      int colEnd = Math.min(col+windowSize+1, sector[0].length);

      for(int i = rowStart; i < rowEnd; i++) {

        System.arraycopy(sector[i], colStart, window[i-rowStart], 0, colEnd-colStart);
      }
      return window;
    }
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
