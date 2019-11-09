package distributed.application.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Sector;
import distributed.common.wireformats.GenericMessage;
import distributed.common.wireformats.GetSectorRequest;
import distributed.common.wireformats.Protocol;

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
  
  public String getServer(Sector sector) throws IOException {
	  if(availableSectors.containsKey(sector)) {
		  // TODO load balance servers
		  Set<String> servers = availableSectors.get(sector);
		  return servers.iterator().next();
	  } else {
		  // return random server
		  // TODO load balance servers
		  List<String> servers = new ArrayList<>(serverConnections.keySet());
		  Collections.shuffle(servers);
		  String server = servers.get(0);
		  
		  // Instruct server to pull new sector
		  GetSectorRequest request = new GetSectorRequest(Protocol.GET_SECTOR_REQUEST, sector);
		  serverConnections.get(server).getConnection().getTCPSender().sendData( request.getBytes() );
		 
		  return (server);
	  }
  }
  
  private String getServerFewestThreads() {
	  return serverConnections.entrySet()
			  .stream()
			  .sorted((e1,e2) -> e1.getValue().getThreadCount() - e2.getValue().getThreadCount())
			  .findFirst()
			  .get()
			  .getKey();
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
    
    // add sectors to map
    for(String sector: message.getSectorIdentifiers()) {
    	if(availableSectors.containsKey(sector)) {
    		availableSectors.get(sector).add(message.getIdentifier());
    	} else {
    		availableSectors.put(sector, new HashSet<>(Arrays.asList(message.getIdentifier())));
    	}
    }
    
  }

}
