package distributed.application.node;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
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
import distributed.common.wireformats.Event;
import distributed.common.wireformats.GenericMessage;
import distributed.common.wireformats.GenericSectorMessage;
import distributed.common.wireformats.Protocol;
import distributed.common.wireformats.SectorWindowRequest;
import distributed.common.wireformats.SectorWindowResponse;

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

      case Protocol.GET_SECTOR_REQUEST :
        getSectorRequestHandler( event, connection );
        break;

      case Protocol.SECTOR_WINDOW_REQUEST :
        handleSectorWindowRequest( event, connection );
        break;
    }
  }

  private void handleSectorWindowRequest(Event event, TCPConnection connection) {
    SectorWindowRequest request = ( SectorWindowRequest ) event;
    Set<Sector> matchingSectors = metadata.getMatchingSectors( request.getSectors() );
    byte[][] window = metadata.getWindow( matchingSectors, request.currentSector, request.position[ 0 ],
        request.position[ 1 ], request.windowSize );
    try
    {
      connection.getTCPSender().sendData(
          new SectorWindowResponse( Protocol.SECTOR_WINDOW_RESPONSE, window, request.getSectors().size() ).getBytes() );
    } catch ( IOException e )
    {
      LOG.error( "Unable to reply to Client for window request. " + e.toString() );
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param sector
   */
  private void loadFile(Sector sector) {
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
      connection.getTCPSender().sendData( new GenericMessage( Protocol.SERVER_INITIALIZED,
          metadata.getIdentifier() + Constants.SEPERATOR + request.sector.toString() ).getBytes() );
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
