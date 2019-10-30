package application.system.metadata;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ServerMetadata {

  private final String host;

  private final int port;

  /**
   * Default Constructor -
   * 
   */
  public ServerMetadata(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getConnection() {
    return host + ":" + port;
  }

}
