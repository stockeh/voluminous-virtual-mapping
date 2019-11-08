package distributed.application.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  // server identifier, server information
  private final Map<String, ServerInformation> serverConnections;
  private final Map<String, Set<String>> availableSectors;

  private final String identifier;

  /**
   * Default Constructor -
   * 
   */
  public SwitchMetadata(String host, int port) {
    this.serverConnections = new HashMap<>();
    this.identifier = host + ":" + port;
    this.availableSectors = new HashMap<>();
  }

  /**
   * 
   * @return
   */
  public String getIdentifier() {
    return identifier;
  }
  
  public String getServer(String sector) {
	  if(availableSectors.containsKey(sector)) {
		  // TODO load balance servers
		  Set<String> servers = availableSectors.get(sector);
		  return servers.iterator().next();
	  } else {
		  // return random server
		  // TODO load balance servers
		  List<String> servers = new ArrayList<>(serverConnections.keySet());
		  Collections.shuffle(servers);
		  return (servers.get(0));
	  }
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
