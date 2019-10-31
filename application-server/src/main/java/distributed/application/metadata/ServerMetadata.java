package distributed.application.metadata;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class ServerMetadata {

  private final String identifier;

  /**
   * Default Constructor -
   * 
   */
  public ServerMetadata(String host, int port) {
    this.identifier = host + ":" + port;
  }

  /**
   * 
   * @return
   */
  public String getIdentifier() {
    return identifier;
  }

}
