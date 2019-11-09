package distributed.application.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
  private ConcurrentHashMap<String, SectorInformation> sectors;

  public void addSector(String sectorID, byte[][] sector) {
    sectors.put(sectorID, new SectorInformation(sector));
  }

  public boolean containsSector(String sectorID) {
    return sectors.containsKey(sectorID);
  }
  
  /**
   * Default Constructor -
   * 
   */
  public ServerMetadata(String host, int port) {
    this.identifier = host + ":" + port;
    this.sectors = new ConcurrentHashMap<>();
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
  public Set<String> getSectorIdentifiers() {
    return sectors.keySet();
  }

}
