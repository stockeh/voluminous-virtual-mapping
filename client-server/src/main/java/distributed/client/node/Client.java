package distributed.client.node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;

import distributed.client.metadata.ClientMetadata;
import distributed.client.util.Properties;
import distributed.client.wireformats.EventFactory;
import distributed.common.node.Node;
import distributed.common.transport.TCPConnection;
import distributed.common.transport.TCPSender;
import distributed.common.transport.TCPServerThread;
import distributed.common.util.Functions;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.DiscoverResponse;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.GenericMessage;
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

    String temp = System.getProperty("user.home");
    logDirectory = Properties.SECTOR_LOGGING_DIR+"_"+temp.substring(temp.lastIndexOf('/')+1);
    logFile = metadata.getConnection()+".log";
  }


  private void createLoggingDir() {

    Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rwxrwxrwx");
    FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
    try {
      Path path = Paths.get(logDirectory);
      LOG.info("Setting up logging directory at " + path);
      Functions.deleteDirectory(path);
      Files.createDirectory(path, permissions);
      Files.setPosixFilePermissions(path, ownerWritable);
    }catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void logToDir(String filename, String data) {
    logToDir(filename, data.getBytes());
  }

  private void logToDir(String fileName, byte[] content) {
    Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rw-rw-rw-");
    FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
    if(!fileName.startsWith("/")) fileName = '/' + fileName;
    Path path = Paths.get(logDirectory + fileName);

    try {
      if(Files.notExists(path)) Files.createFile(path, permissions);
      Files.setPosixFilePermissions(path, ownerWritable);
      Files.write(
              path,
              content,
              StandardOpenOption.APPEND);
    } catch (IOException e) {
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
      node.createLoggingDir();
      LOG.info( "Client node starting up at: " + new Date() + ", on "
          + node.metadata.getConnection() );

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

      sender.sendData( new GenericMessage( Protocol.DISCOVER_REQUEST,
          metadata.getNavigator().getInitialSector().toString() ).getBytes() );

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
      case Protocol.DISCOVER_RESPONSE :
        connectToServer( event, connection );
        break;

      case Protocol.REGISTER_CLIENT_RESPONSE :
        LOG.info( "Client successfully connected to the server!" );
        ( new Thread( metadata.getNavigator(), "Navigation Thread" ) ).start();
        break;

      case Protocol.SERVER_INITIALIZED :
        LOG.info( "The initial server has successfully loaded the file. "
            + "Initializing Client." );
        break;

      case Protocol.SECTOR_WINDOW_RESPONSE :
        handelSectorWindowResponse( event );
        break;
    }
  }

  /**
   * Manage the response from the server(s) containing the bytes of the
   * requested window.
   * 
   * @param event
   */
  private void handelSectorWindowResponse(Event event) {
    SectorWindowResponse response = ( SectorWindowResponse ) event;

    // TODO: wait for all responses to come in before constructing the
    // window and then write to file?

//    LOG.debug( response.numSectors + " -- " );
//    LOG.info("Logging sector of size " + response.sectorWindow.length + " to " + Properties.SECTOR_LOGGING_DIR + "sector.log");
    for(byte[] row : response.sectorWindow) {
      logToDir(logFile, row);
    }
    logToDir(logFile, "\n");
  }

  /**
   * Initiate a connection with the server and close the connection to
   * the switch.
   * 
   * @param event
   * @param connection
   */
  private void connectToServer(Event event, TCPConnection connection) {
    DiscoverResponse response = ( DiscoverResponse ) event;

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

      server.getTCPSender()
          .sendData( new GenericMessage( Protocol.REGISTER_CLIENT_REQUEST,
              metadata.getNavigator().getInitialSector().toString() )
                  .getBytes() );

      metadata.getNavigator().setInitialServerConnection( server );

    } catch ( IOException e )
    {
      LOG.error( "Unable to connect client to Server " + e.toString() );
      e.printStackTrace();
      System.exit( 1 );
    }
    connection.close();
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
