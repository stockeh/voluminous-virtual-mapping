package distributed.client.metadata;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ClientMetadata {

  private final String host;

  private final int port;

  /**
   * Default Constructor -
   * 
   */
  public ClientMetadata(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getConnection() {
    return host + ":" + port;
  }

}
