package distributed.client.node;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import distributed.client.metadata.ClientMetadata;
import distributed.client.util.Properties;
import distributed.client.wireformats.EventFactory;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPSender;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.ClientDiscoverRequest;
import distributed.common.wireformats.ClientDiscoverResponse;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.Protocol;
import distributed.common.wireformats.SectorWindowResponse;

/**
 *
 * @author stock
 *
 */
public class Client implements Node {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private static final String EXIT = "exit";

  private static final String HELP = "help";

  private final ClientMetadata metadata;

  private final String logDirectory;
  private final String logFile;

  /**
   * Default constructor - creates a new server tying the
   * <b>host:port</b> combination for the node as the identifier for
   * itself.
   * 
   * @param host
   * @param port
   * @param args
   * 
   * @throws NumberFormatException
   * @throws IndexOutOfBoundsException
   */
  private Client(String host, int port, String[] args)
      throws NumberFormatException, IndexOutOfBoundsException {

    Sector initialSector = new Sector();
    if ( args.length > 0 )
    {
      String[] s = args[ 0 ].split( "," );
      initialSector.update( Integer.parseInt( s[ 0 ] ),
          Integer.parseInt( s[ 1 ] ) );
    }
    int[] initialPosition = new int[] { 0, 0 };
    if ( args.length > 1 )
    {
      String[] s = args[ 1 ].split( "," );
      initialPosition[ 0 ] = Integer.parseInt( s[ 0 ] );
      initialPosition[ 1 ] = Integer.parseInt( s[ 1 ] );
    }

    this.metadata = new ClientMetadata( host, port );
    this.metadata.setNavigation( initialSector, initialPosition );

    String temp = System.getProperty( "user.home" );
    logDirectory = Properties.SECTOR_LOGGING_DIR + "_"
        + temp.substring( temp.lastIndexOf( File.separator ) + 1 );
    logFile = metadata.getConnection() + ".log";
  }


  private void createLoggingDir() {

    Set<PosixFilePermission> ownerWritable =
        PosixFilePermissions.fromString( "rwxrwxrwx" );
    FileAttribute<?> permissions =
        PosixFilePermissions.asFileAttribute( ownerWritable );
    try
    {
      Path path = Paths.get( logDirectory );
      LOG.info( "Setting up logging directory at " + path );
      // Functions.deleteDirectory( path );
      if ( !Files.isDirectory( path ) )
      {
        Files.createDirectory( path, permissions );
        Files.setPosixFilePermissions( path, ownerWritable );
      }
      Path logPath = Paths.get( logDirectory + File.separator + logFile );
      LOG.info( "Writing log file at " + logPath );
      Files.deleteIfExists( logPath );
      Files.createFile( logPath, permissions );

      Files.setPosixFilePermissions( logPath, ownerWritable );
    } catch ( IOException e )
    {
      e.printStackTrace();
    }
  }

  private void logToDir(String filename, String data) {
    logToDir( filename, data.getBytes() );
  }

  private void logToDir(String fileName, byte[] content) {

    if ( !fileName.startsWith( File.separator ) )
    {
      fileName = File.separator + fileName;
    }
    Path path = Paths.get( logDirectory + fileName );

    try
    {
      Files.write( path, content, StandardOpenOption.APPEND );
    } catch ( IOException e )
    {
      LOG.error( "Unable to write to \tmp logs. " + e.toString() );
      e.printStackTrace();
    }
  }

  /**
   * Start listening for incoming connections and establish connection
   * into the server network.
   *
   * @param args
   */
  public static void main(String[] args) {
    try ( ServerSocket serverSocket = new ServerSocket( 0 ) )
    {
      Client node = new Client( InetAddress.getLocalHost().getHostName(),
          serverSocket.getLocalPort(), args );

      LOG.info( "Client node starting up at: " + new Date() + ", on "
          + node.metadata.getConnection() + " in "
          + node.metadata.getNavigator() );

      node.createLoggingDir();

      ( new Thread(
          new TCPServerThread( node, serverSocket, EventFactory.getInstance() ),
          "Client Thread" ) ).start();

      node.connectToSwitch( args );
      node.interact();
    } catch ( IOException | NumberFormatException
        | IndexOutOfBoundsException e )
    {
      LOG.error(
          "Unable to successfully start server. Exiting. " + e.toString() );
      System.exit( 1 );
    }
  }

  /**
   * Send connection message to switch to request a server
   * 
   * @param args contains the sector location
   * 
   */
  public void connectToSwitch(String[] args) {
    try
    {
      TCPConnection switchConnection = new TCPConnection( this,
          new Socket( Properties.SWITCH_HOST, Properties.SWITCH_PORT ),
          EventFactory.getInstance() );

      switchConnection.startReceiver();

      TCPSender sender = switchConnection.getTCPSender();

      sender.sendData(
          new ClientDiscoverRequest( Protocol.CLIENT_DISCOVER_REQUEST,
              metadata.getNavigator().getInitialSector(),
              metadata.getConnection() ).getBytes() );

    } catch ( IOException e )
    {
      LOG.error( "Unable to connect to the Switch. " + e.toString() );
      e.printStackTrace();
    }
    return;
  }

  /**
   * Allow support for commands to be specified while the processes are
   * running.
   * 
   */
  private void interact() {
    System.out.println(
        "\nInput a command to interact with processes. Input 'help' for a "
            + "list of commands.\n" );
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
          LOG.error(
              "Unable to process. Please enter a valid command! Input 'help'"
                  + " for options." );
          break;
      }
    }
    LOG.info(
        metadata.getConnection() + " has unregistered and is terminating." );
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
      case Protocol.CLIENT_DISCOVER_RESPONSE :
        connectToServer( event, connection );
        break;

      case Protocol.SECTOR_WINDOW_RESPONSE :
        handleSectorWindowResponse( event );
        break;
    }
  }

  /**
   * Manage the response from the server(s) containing the bytes of the
   * requested window.
   * 
   * @param event
   */
  private void handleSectorWindowResponse(Event event) {
    SectorWindowResponse response = ( SectorWindowResponse ) event;

    // TODO: wait for all responses to come in before constructing the
    // window and then write to file?

    // LOG.debug( response.numSectors + " -- " );
    // LOG.info("Logging sector of size " + response.sectorWindow.length +
    // " to " + Properties.SECTOR_LOGGING_DIR + "sector.log");
    Date date = new Date( response.initialTimestamp );
    DateFormat formatter = new SimpleDateFormat( "HH:mm:ss.SSS" );
    formatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

    String dateFormatted = formatter.format( date );

    LOG.info( String.format( "%s-Sector: %s Sector Size:%dx%d", dateFormatted,
        response.sectorID, response.sectorWindow.length,
        response.sectorWindow[ 0 ].length ) );
    for ( byte[] row : response.sectorWindow )
    {
      logToDir( logFile, row );
    }
    logToDir( logFile, "\n" );
    // LOG.debug( response.numSectors + " -- " );
    // LOG.info("Logging sector of size " + response.sectorWindow.length +
    // " to " + Properties.SECTOR_LOGGING_DIR + "sector.log");
  }

  /**
   * Initiate a connection with the server and close the connection to
   * the switch.
   * 
   * @param event
   * @param connection
   */
  private void connectToServer(Event event, TCPConnection connection) {
    ClientDiscoverResponse response = ( ClientDiscoverResponse ) event;

    String[] connectionIdentifier = response.serverToConnect.split( ":" );
    metadata.getNavigator().setSectorMapSize( response.mapSize );
    metadata.getNavigator().setSectorBoundarySize( response.sectorSize );

    try
    {
      TCPConnection server = new TCPConnection( this,
          new Socket( connectionIdentifier[ 0 ],
              Integer.parseInt( connectionIdentifier[ 1 ] ) ),
          EventFactory.getInstance() );

      server.startReceiver();

      metadata.getNavigator().setInitialServerConnection( server );

    } catch ( IOException e )
    {
      LOG.error( "Unable to connect client to Server " + e.toString() );
      e.printStackTrace();
      System.exit( 1 );
    }
    connection.close();

    LOG.info( "Client successfully connected to the server: "
        + response.serverToConnect );
    ( new Thread( metadata.getNavigator(), "Navigation Thread" ) ).start();
  }

  /**
   * Display a help message for how to interact with the application.
   * 
   */
  private void displayHelp() {
    StringBuilder sb = new StringBuilder();

    sb.append( "\n\t" ).append( EXIT )
        .append( "\t\t: gracefully leave the network.\n" );

    System.out.println( sb.toString() );
  }

}
