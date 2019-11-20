package distributed.client.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import distributed.client.util.Navigator;
import distributed.common.util.Sector;
import distributed.common.wireformats.SectorWindowResponse;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ClientMetadata {

  private final String connection;

  private final String host;

  private final int port;

  private Navigator navigator;

  private final HashMap<Long, List<SectorWindowResponse>> responseMap = new HashMap<>();

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  /**
   * Default Constructor -
   * 
   */
  public ClientMetadata(String host, int port) {
    this.host = host;
    this.port = port;
    this.connection = host + ":" + port;
  }

  /**
   * 
   * @return
   */
  public String getConnection() {
    return connection;
  }

  public Navigator getNavigator() {
    return navigator;
  }

  public void setNavigation(Sector initialSector, int[] initialPosition) {
    navigator = new Navigator( initialSector, initialPosition, port );
  }

  public boolean addResponse(SectorWindowResponse response) {
    synchronized (responseMap) {
      responseMap.putIfAbsent(response.initialTimestamp, new ArrayList<>());
      responseMap.get(response.initialTimestamp).add(response);
      return responseMap.get(response.initialTimestamp).size() == response.numSectors;
    }
  }

  public List<SectorWindowResponse> getAndRemove(long timestamp) {
    synchronized (responseMap) {
      return responseMap.remove(timestamp);
    }
  }

}
