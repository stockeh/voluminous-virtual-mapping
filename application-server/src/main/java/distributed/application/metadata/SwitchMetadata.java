package distributed.application.metadata;

import java.util.HashMap;
import java.util.Map;
import distributed.common.transport.TCPConnection;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class SwitchMetadata {

  private final Map<String, TCPConnection> serverConnections;

  private final String connection;

  /**
   * Default Constructor -
   * 
   */
  public SwitchMetadata(String host, int port) {
    this.serverConnections = new HashMap<>();
    this.connection = host + ":" + port;
  }

  /**
   * 
   * @return
   */
  public String getConnection() {
    return connection;
  }

  /**
   * 
   * @return
   */
  public Map<String, TCPConnection> getServerConnections() {
    return serverConnections;
  }

  /**
   * 
   * @param identifier
   * @param connection
   * @return
   */
  public TCPConnection addServerConnection(String identifier,
      TCPConnection connection) {
    return serverConnections.put( identifier, connection );
  }

}
