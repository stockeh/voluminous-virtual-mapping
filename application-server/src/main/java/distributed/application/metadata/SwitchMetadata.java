package distributed.application.metadata;

import java.util.HashMap;
import java.util.Map;
import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class SwitchMetadata {

  private final Map<String, ServerInformation> serverConnections;

  private final String identifier;

  /**
   * Default Constructor -
   * 
   */
  public SwitchMetadata(String host, int port) {
    this.serverConnections = new HashMap<>();
    this.identifier = host + ":" + port;
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
  public Map<String, ServerInformation> getServerConnections() {
    return serverConnections;
  }

  /**
   * 
   * @param identifier
   * @param connection
   * @return
   */
  public ServerInformation addServerConnection(String identifier,
      TCPConnection connection) {
    return serverConnections.put( identifier,
        new ServerInformation( connection ) );
  }

  /**
   * 
   * @param message
   */
  public void processApplicationHeatbeat(ApplicationHeartbeat message) {
    ServerInformation info = serverConnections.get( message.getIdentifier() );
    info.updateServerInformation( message );
  }

}
