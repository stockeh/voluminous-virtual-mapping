package distributed.application.metadata;

import java.io.IOException;
import java.util.*;

import distributed.application.util.Constants;
import distributed.application.util.Properties;
import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
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
  private final List<String> serverSet;
  // sector identifier, server identifier
  private final Map<Sector, Set<String>> availableSectors;
  // server\tsector identifier, client connections
  private final Map<String, List<TCPConnection>> clientConnections;

  private final Comparator<String> cSectors;
  private final Comparator<String> cThreads;
  private final Comparator<String> cBoth;

//  private static final Comparator<Map.Entry<String,ServerInformation>> cSectors = Comparator.comparing( s -> s.getValue().getNumSectors());
//  private static final Comparator<Map.Entry<String,ServerInformation>> cRand = Comparator.comparing( s -> s.getValue().getRandomComparable());
//  private static final Comparator<Map.Entry<String,ServerInformation>> cBoth = cSectors.thenComparing(cRand);

  private final String identifier;

  /**
   * Default Constructor -
   * 
   */
  public SwitchMetadata(String host, int port) {
    this.serverConnections = new HashMap<>();
    this.availableSectors = new HashMap<>();
    this.clientConnections = new HashMap<>();
    this.serverSet = new ArrayList<>();
    cSectors = Comparator.comparingInt( s -> serverConnections.get(s).getNumSectors());
    cThreads = Comparator.comparingInt(s -> serverConnections.get(s).getThreadCount());
    cBoth = cThreads.thenComparing(cSectors);
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
    if(server == null) return;
    TCPConnection serverConnection = serverConnections.get(server).getConnection();
    request.loadSector = loadSectors;
    try {
      serverConnection.getTCPSender().sendData(request.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized String getSectorDestination(Sector sector) {

//    List<Map.Entry<String, ServerInformation>> serverSet = new ArrayList<>(serverConnections.entrySet());

    serverSet.sort(cBoth);

    String server = serverSet.remove(0);
    serverSet.add(server);
    if(serverConnections.get(server).getThreadCount() >= Properties.MAX_SERVER_THREADS) return null;
    availableSectors.put(sector, new HashSet<>(Arrays.asList( server )));
    return server;
  }

  public synchronized String instructServerToPullSector(Sector sector, TCPConnection clientConnection) throws IOException {
    String server = getSectorDestination(sector);
    if(server == null) return "";
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

  public synchronized String getServer(Sector sector, TCPConnection clientConnection)
      throws IOException {
    if ( availableSectors.containsKey( sector ) )
    {

      // TODO load balance servers
      List<String> servers = new ArrayList<>(availableSectors.get( sector ));
      servers.sort(cBoth);
      String server = servers.get(0);
      if(serverConnections.get(server).getThreadCount() >= Properties.MAX_SERVER_THREADS) return instructServerToPullSector(sector, clientConnection);

      if(clientConnections.containsKey(server+Constants.SEPERATOR+sector.toString())) {
        addClientConnection( sector, server, clientConnection );
        return null;
      }
      return addClientConnection( sector, server, clientConnection );
    } else
    {
      // return random server
      // TODO load balance servers
     return instructServerToPullSector(sector, clientConnection);
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
    serverSet.add(identifier);
    return serverConnections.put( identifier,
        new ServerInformation( connection ) );
  }

  public TCPConnection getTCPConnection(String server) {
    return serverConnections.get(server).getConnection();
  }


  private void logConnections() {
    LOG.debug("HEARTBEAT:\n");
    for(Map.Entry<String, ServerInformation> entry : serverConnections.entrySet()) {
      LOG.debug("SERVER INFO: " + entry.getValue().toString());
    }
    LOG.debug("\n");
  }
  /**
   * 
   * @param message
   */
  public synchronized void processApplicationHeatbeat(
      ApplicationHeartbeat message) {
    ServerInformation info = serverConnections.get( message.getIdentifier() );
    info.updateServerInformation( message );
    serverConnections.put( message.getIdentifier(), info);

    // add sectors to map
    for ( Sector sector : message.getSectorIdentifiers() )
    {
      if ( availableSectors.containsKey( sector ) )
      {
        availableSectors.get( sector ).add( message.getIdentifier() );
      } else
      {
        availableSectors.put( sector,
            new HashSet<>(Collections.singletonList(message.getIdentifier())) );
      }
    }

    logConnections();

  }

}
