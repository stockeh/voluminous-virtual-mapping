package distributed.application.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
  private Map<String, SectorInformation> sectors;

  
  
  /**
   * Default Constructor -
   * 
   */
  public ServerMetadata(String host, int port) {
    this.identifier = host + ":" + port;
    this.sectors = new HashMap<>();
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
