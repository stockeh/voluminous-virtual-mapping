package distributed.application.node;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import distributed.application.heartbeat.ServerHeartbeatManager;
import distributed.application.io.DFS;
import distributed.application.metadata.ServerMetadata;
import distributed.application.util.Constants;
import distributed.application.util.Functions;
import distributed.application.util.ProcessCpuTicksGauge;
import distributed.application.util.Properties;
import distributed.application.wireformats.EventFactory;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.*;

/**
 *
 * @author stock
 *
 */
public class Server implements Node {

  private static final Logger LOG = Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private static final String EXIT = "exit";

  private static final String HELP = "help";

  private final ServerMetadata metadata;

  private TCPConnection switchConnection;
  // Metrics
  static final MetricRegistry metrics = new MetricRegistry();


  /**
   * Default constructor - creates a new server tying the
   * <b>host:port</b> combination for the node as the identifier for
   * itself.
   * 
   * @param host
   * @param port
   */
  private Server(String host, int port) {
    this.metadata = new ServerMetadata( host, port );
  }

  /**
   * Start listening for incoming connections and establish connection
   * into the server network.
   *
   * @param args
   * @throws UnknownHostException
   */
  public static void main(String[] args) throws UnknownHostException {
    // metrics
    startMetrics();

    try ( ServerSocket serverSocket = new ServerSocket( 0 ) )
    {
      Server node = new Server( InetAddress.getLocalHost().getHostName(), serverSocket.getLocalPort() );

      LOG.info( "Server node starting up at: " + new Date() + ", on " + node.metadata.getIdentifier() );

      ( new Thread( new TCPServerThread( node, serverSocket, EventFactory.getInstance() ), "Server Thread" ) ).start();

      node.discoverSwitchConnection();

      node.interact();
    } catch ( IOException e )
    {
      LOG.error( "Unable to successfully start server. Exiting. " + e.toString() );
      System.exit( 1 );
    }
  }
  
  private static void startMetrics() throws UnknownHostException {
    // metrics
    // metrics.register( "gc", new GarbageCollectorMetricSet() );
    metrics.register( "threads", new CachedThreadStatesGaugeSet( 10, TimeUnit.SECONDS ) );
    metrics.register( "memory", new MemoryUsageGaugeSet() );

    try
    {
      metrics.register( "jvm.process.cpu.seconds", new ProcessCpuTicksGauge() );
    } catch ( ClassNotFoundException | IOException e )
    {
      LOG.error( "Unable to load ProcessCpuTicksGauge class for CPU metrics. " + e.toString() );
      e.printStackTrace();
    }

    // ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
    // .convertRatesTo(TimeUnit.SECONDS)
    // .convertDurationsTo(TimeUnit.MILLISECONDS)
    // .build();
    // reporter.start(1, TimeUnit.SECONDS);

    File dir = new File(
        System.getProperty( "user.home" ) + "/vvm/servers/" + InetAddress.getLocalHost().getHostName() + "/");

    if( dir.exists()) {
      distributed.common.util.Functions.deleteDirectory(dir.toPath());
    }
    if ( !dir.exists() && !dir.mkdirs() )
    {
      LOG.error( "Cannot create directory " + dir.getAbsoluteFile() );
    }

    LOG.info( "Created dir " + dir );

    CsvReporter reporter = CsvReporter.forRegistry( metrics ).formatFor( Locale.US ).convertRatesTo( TimeUnit.SECONDS )
        .convertDurationsTo( TimeUnit.MILLISECONDS ).build( dir );
    reporter.start( 1, TimeUnit.SECONDS );
  }

  /**
   * Connect the server to the switch
   * 
   * @throws IOException if unable to connect to the switch, and thus,
   *         the network
   */
  private void discoverSwitchConnection() throws IOException {
    Socket socketToSwitch = new Socket( Properties.SWITCH_HOST, Properties.SWITCH_PORT );
    TCPConnection connection = new TCPConnection( this, socketToSwitch, EventFactory.getInstance() );
    connection.startReceiver();

    GenericMessage request = new GenericMessage( Protocol.REGISTER_SERVER_REQUEST, metadata.getIdentifier() );
    connection.getTCPSender().sendData( request.getBytes() );

    switchConnection = connection;

    ServerHeartbeatManager serverHeartbeatManager = new ServerHeartbeatManager( connection, metadata );
    Timer timer = new Timer();
    // 15 seconds intervals in milliseconds
    timer.schedule( serverHeartbeatManager, 1000, 15000 );
  }

  /**
   * Allow support for commands to be specified while the processes are
   * running.
   * 
   */
  private void interact() {
    System.out.println( "\nInput a command to interact with processes. Input 'help' for a " + "list of commands.\n" );
    boolean running = true;
    while ( running )
    {
      @SuppressWarnings( "resource" )
      Scanner scan = new Scanner( System.in );
      String[] input = scan.nextLine().toLowerCase().split( "\\s+" );
      switch ( input[ 0 ] )
      {

        case EXIT :
          running = false;
          break;

        case HELP :
          displayHelp();
          break;

        default :
          LOG.error( "Unable to process. Please enter a valid command! Input 'help'" + " for options." );
          break;
      }
    }
    LOG.info( metadata.getIdentifier() + " has unregistered and is terminating." );
    System.exit( 0 );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onEvent(Event event, TCPConnection connection) {
    LOG.debug( event.toString() );
    switch ( event.getType() )
    {
      case Protocol.REGISTER_SERVER_RESPONSE :
        registerServerResponseHandler( event, connection );
        break;

      case Protocol.LOAD_SECTOR:
      case Protocol.GET_SECTOR_REQUEST :
        getSectorRequestHandler( event, connection );
        break;

      case Protocol.SECTOR_WINDOW_REQUEST :
        handleSectorWindowRequest( event, connection );
        break;
    }
  }

  private void forwardSectorWindowRequests(Set<Sector> sectors, SectorWindowRequest request, TCPConnection connection) {
    request.sectors = sectors;
    request.host = connection.getEndHost();
    try {
      switchConnection.getTCPSender().sendData(request.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }



  private void handleSectorPrefetching(Set<Sector> mySectors, int[] position, int windowSize, Sector current) {
    Set<Sector> toPrefetch = new HashSet<>();
    int preMult = Properties.SECTOR_PREFETCH_MULTIPLIER*windowSize;
    if(position[0]+preMult >= Properties.SECTOR_BOUNDARY_SIZE) {
      int x = current.x == Properties.SECTOR_MAP_SIZE-1 ? 0 : current.x+1;
      int y = current.y;
      Sector sector = new Sector(x, y);
      if(!toPrefetch.contains(sector)) {
        toPrefetch.add(sector);
      }
      if(position[1]+preMult >= Properties.SECTOR_BOUNDARY_SIZE) {
        y = current.y == Properties.SECTOR_MAP_SIZE-1 ? 0 : current.y+1;
        sector = new Sector(x, y);
        if(!mySectors.contains(sector)) {
          toPrefetch.add(sector);
        }
      }
    }

    if(position[0]-preMult <= 0) {
      int x = current.x == 0 ? Properties.SECTOR_MAP_SIZE-1 : current.x-1;
      int y = current.y;
      Sector sector = new Sector(x, y);
      if(!toPrefetch.contains(sector)) {
        toPrefetch.add(sector);
      }
      if(position[1]-preMult <= 0) {
        y = current.y == 0 ? Properties.SECTOR_MAP_SIZE-1 : current.y-1;
        sector = new Sector(x, y);
        if(!mySectors.contains(sector)) {
          toPrefetch.add(sector);
        }
      }
    }

    if(position[1]+preMult >= Properties.SECTOR_BOUNDARY_SIZE) {
      int x = current.x;
      int y = current.y == Properties.SECTOR_MAP_SIZE-1 ? 0 : current.y+1;
      Sector sector = new Sector(x, y);
      if(!mySectors.contains(sector)) {
        toPrefetch.add(sector);
      }
      if(position[0]+preMult >= Properties.SECTOR_BOUNDARY_SIZE) {
        x = current.x == Properties.SECTOR_MAP_SIZE-1 ? 0 : current.x+1;
        sector = new Sector(x, y);
        if(!mySectors.contains(sector)) {
          toPrefetch.add(sector);
        }
      }
    }

    if(position[1]-preMult <= 0) {
      int x = current.x;
      int y = current.y == 0 ? Properties.SECTOR_MAP_SIZE-1 : current.y-1;
      Sector sector = new Sector(x, y);
      if(!mySectors.contains(sector)) {
        toPrefetch.add(sector);
      }
      if(position[0]-preMult <= 0) {
        x = current.x == 0 ? Properties.SECTOR_MAP_SIZE-1  : current.x-1;
        sector = new Sector(x, y);
        if(!mySectors.contains(sector)) {
          toPrefetch.add(sector);
        }
      }
    }

    try {
      switchConnection.getTCPSender().sendData(new PrefetchSectorRequest(Protocol.PREFETCH_SECTORS, toPrefetch).getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleSectorWindowRequest(Event event,
      TCPConnection connection) {
    TCPConnection clientConnection;
    SectorWindowRequest request = (SectorWindowRequest) event;
    if (request.loadSector) {
      for (Sector toLoad : request.sectors) {
        loadFile(toLoad);
      }
    }

    if (!request.host.isEmpty()) {
      try {
        LOG.info("Creating new connection with client: " + request.host + ":" + request.port);
        clientConnection = new TCPConnection(this, new Socket(request.host, request.port), EventFactory.getInstance());
        clientConnection.startReceiver();

      } catch (IOException e) {
        clientConnection = connection;
        e.printStackTrace();
      }
    } else {
      clientConnection = connection;
    }

    Set<Sector> matchingSectors =
            metadata.getMatchingSectors(request.getSectors());
    Set<Sector> nonMatchingSectors = metadata.getNonMatchingSectors(request.getSectors());
    for (Sector sector : matchingSectors) {
      byte[][] window =
              metadata.getWindow(sector, request.currentSector,
                      request.position[0], request.position[1], request.windowSize);
      try {
        clientConnection.getTCPSender()
                .sendData(new SectorWindowResponse(Protocol.SECTOR_WINDOW_RESPONSE,
                        window, request.getSectors().size(), request.initialTimestamp, sector).getBytes());
      } catch (IOException e) {
        LOG.error("Unable to reply to Client for window request. " + e.toString());
        e.printStackTrace();
      }
    }
    forwardSectorWindowRequests(nonMatchingSectors, request, connection);
    handleSectorPrefetching(matchingSectors, request.position, request.windowSize, request.currentSector);
  }

  /**
   * 
   * @param sector
   */
  private void loadFile(Sector sector) {
    LOG.info("Loading Sector: " + sector);
    if ( metadata.containsSector( sector ) )
    {
      LOG.info( "Server already contains sector, not reloading" );
      return;
    }
    try
    {
      String filename = Properties.HDFS_FILE_LOCATION;
      byte[][] bytes = Functions.reshape( DFS.readFile( filename ) );
      metadata.addSector( sector, bytes );
    } catch ( IOException e )
    {
      LOG.error( "Unable to load the sector file from the data store. " + e.toString() );
      e.printStackTrace();
    }
  }



  /**
   * 
   * @param event
   * @param connection
   */
  private void getSectorRequestHandler(Event event, TCPConnection connection) {
    GenericSectorMessage request = ( GenericSectorMessage ) event;
    LOG.info( "Loading Sector: " + request.sector );
    loadFile( request.sector );

    try
    {
      int type = request.type == Protocol.GET_SECTOR_REQUEST ? Protocol.SERVER_INITIALIZED : Protocol.SECTOR_LOADED;
      connection.getTCPSender()
          .sendData( new GenericMessage(  type,
              metadata.getIdentifier() + Constants.SEPERATOR
                  + request.sector.toString() ).getBytes() );
    } catch ( IOException e )
    {
      LOG.error( "Unable to respond to Switch for load file request. " + e.toString() );
      e.printStackTrace();
    }
  }

  /**
   * Display the response status from the switch.
   * 
   * @param event
   * @param connection
   */
  private void registerServerResponseHandler(Event event, TCPConnection connection) {
    if ( Boolean.parseBoolean( ( ( GenericMessage ) event ).getMessage() ) == Constants.SUCCESS )
    {
      LOG.info( "The server successfuly connected with the switch!" );
    } else
    {
      LOG.error( "The server was NOT able to connect with the switch successfully. Exiting!" );
      connection.close();
      System.exit( 1 );
    }
  }

  /**
   * Display a help message for how to interact with the application.
   * 
   */
  private void displayHelp() {
    StringBuilder sb = new StringBuilder();

    sb.append( "\n\t" ).append( EXIT ).append( "\t\t: gracefully leave the network.\n" );

    System.out.println( sb.toString() );
  }

}
