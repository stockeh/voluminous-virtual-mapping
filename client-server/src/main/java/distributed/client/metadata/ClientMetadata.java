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
  private final String host;
  private final int port;

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

}
