package distributed.application.metadata;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import distributed.application.node.Server;
import distributed.application.util.Constants;
import distributed.application.util.Properties;
import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.GenericSectorMessage;
import distributed.common.wireformats.Protocol;
import distributed.common.wireformats.SectorWindowRequest;

/**
 * Class to maintain the information needed for a given server. This
 * includes...
 * 
 * @author stock
 *
 */
public class SwitchMetadata {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  // server identifier, server information
  private final Map<String, ServerInformation> serverConnections;
  // sector identifier, server identifier
  private final Map<Sector, Set<String>> availableSectors;
  // server\tsector identifier, client connections
  private final Map<String, List<TCPConnection>> clientConnections;

//  private final Set<String> requestedFiles;

  private final String identifier;

  /**
   * Default Constructor -
   * 
   */
  public SwitchMetadata(String host, int port) {
    this.serverConnections = new HashMap<>();
    this.availableSectors = new HashMap<>();
    this.clientConnections = new HashMap<>();
    this.identifier = host + ":" + port;
  }

  /**
   * 
   * @return
   */
  public String getIdentifier() {
    return identifier;
  }

  public Map<String, List<TCPConnection>> getClientConnections() {
    return clientConnections;
  }

  private String addClientConnection(Sector sector, String server,
      TCPConnection clientConnection) {
    String key = server + Constants.SEPERATOR + sector.toString();
    synchronized ( clientConnections )
    {
      clientConnections.computeIfAbsent( key, v -> new ArrayList<>() )
          .add( clientConnection );
    }
    return key;
  }

  public synchronized boolean sectorIsAvailable(Sector sector) {
    return availableSectors.containsKey(sector);
  }

  public synchronized List<TCPConnection> removeServer(String keyForClientToConnect) {
    return clientConnections.remove( keyForClientToConnect );
  }


  public synchronized void forwardRequestToServer(Sector sector, SectorWindowRequest request) {
    String server;
    boolean loadSectors = false;
    if ( availableSectors.containsKey( sector ) )
    {
      // TODO load balance servers
      Set<String> servers = availableSectors.get( sector );
      server = servers.iterator().next();

      if(clientConnections.containsKey(server+Constants.SEPERATOR+sector.toString())) {
        loadSectors = true;
      }
    } else {
      server = getSectorDestination(sector);
      loadSectors = true;
    }

    TCPConnection serverConnection = serverConnections.get(server).getConnection();
    request.loadSector = loadSectors;
    try {
      serverConnection.getTCPSender().sendData(request.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized String getSectorDestination(Sector sector) {

    List<Map.Entry<String, ServerInformation>> serverSet = new ArrayList<>(serverConnections.entrySet());
    Comparator<Map.Entry<String,ServerInformation>> cSectors = Comparator.comparing( s -> s.getValue().getNumSectors());
    Comparator<Map.Entry<String,ServerInformation>> cRand = Comparator.comparing( s -> s.getValue().getRandomComparable());
    Comparator<Map.Entry<String,ServerInformation>> cBoth = cSectors.thenComparing(cRand);
    serverSet.sort(cBoth);

    String server = serverSet.get(0).getKey();
    availableSectors.put(sector, new HashSet<>(Arrays.asList( server )));
    return server;
  }

  public synchronized String getServer(Sector sector, TCPConnection clientConnection)
      throws IOException {
    String server;
    if ( availableSectors.containsKey( sector ) )
    {

      // TODO load balance servers
      Set<String> servers = availableSectors.get( sector );
      server = servers.iterator().next();

      if(clientConnections.containsKey(server+Constants.SEPERATOR+sector.toString())) {
        addClientConnection( sector, server, clientConnection );
        return null;
      }
      return addClientConnection( sector, server, clientConnection );
    } else
    {
      // return random server
      // TODO load balance servers
      server = getSectorDestination(sector);
      LOG.info( "Instruct " + server + " to pull new sector ( "
          + sector.toString() + " )." );

      // Instruct server to pull new sector
      GenericSectorMessage request =
              new GenericSectorMessage( Protocol.GET_SECTOR_REQUEST, sector );
      serverConnections.get( server ).getConnection().getTCPSender()
          .sendData( request.getBytes() );

      addClientConnection( sector, server, clientConnection );
      return null;
    }
  }

  private synchronized String getServerFewestThreads() {
    return serverConnections.entrySet().stream().sorted( (e1,
        e2) -> e1.getValue().getThreadCount() - e2.getValue().getThreadCount() )
        .findFirst().get().getKey();
  }

  private synchronized String getServerFewestSectors() {
    return serverConnections.entrySet().stream()
        .sorted( (e1, e2) -> e1.getValue().getSectorIdentifiers().size()
            - e2.getValue().getSectorIdentifiers().size() )
        .findFirst().get().getKey();
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
  public synchronized ServerInformation addServerConnection(String identifier,
      TCPConnection connection) {
    return serverConnections.put( identifier,
        new ServerInformation( connection ) );
  }

  /**
   * 
   * @param message
   */
  public synchronized void processApplicationHeatbeat(
      ApplicationHeartbeat message) {
    ServerInformation info = serverConnections.get( message.getIdentifier() );
    info.updateServerInformation( message );

    // add sectors to map
    for ( Sector sector : message.getSectorIdentifiers() )
    {
      if ( availableSectors.containsKey( sector ) )
      {
        availableSectors.get( sector ).add( message.getIdentifier() );
      } else
      {
        availableSectors.put( sector,
            new HashSet<>( Arrays.asList( message.getIdentifier() ) ) );
      }
    }

  }

}
