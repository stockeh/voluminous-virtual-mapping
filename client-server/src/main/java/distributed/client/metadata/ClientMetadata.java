package distributed.client.metadata;

import distributed.client.util.Navigator;
import distributed.common.util.Sector;

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
    navigator = new Navigator( initialSector, initialPosition );
  }

}
