package distributed.client.metadata;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ClientMetadata {

  private final String connection;

  /**
   * Default Constructor -
   * 
   */
  public ClientMetadata(String host, int port) {
    this.connection = host + ":" + port;
  }

  /**
   * 
   * @return
   */
  public String getConnection() {
    return connection;
  }

}
