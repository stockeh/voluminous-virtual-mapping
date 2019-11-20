package distributed.client.util;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Logger;
import distributed.common.util.Sector;
import distributed.common.wireformats.Protocol;
import distributed.common.wireformats.SectorWindowRequest;

/**
 * 
 * @author stock
 *
 */
public class Navigator implements Runnable {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  /**
   * Default to check initialization until client receives update from
   * server
   */
  private int sectorBoundarySize = Integer.MAX_VALUE;
  private int sectorMapSize = Integer.MAX_VALUE;

  private TCPConnection primaryServer;

  private Sector sector;

  private double[] position;

  private double[] velocity;

  public int port;

  /**
   * 
   * @param initialSector
   * @param initialPosition
   */
  public Navigator(Sector initialSector, int[] initialPosition, int port) {
    this.sector = initialSector;

    position = new double[] { initialPosition[ 0 ], initialPosition[ 1 ] };
    velocity = new double[] { 0, 0 };

    this.port = port;

    checkBoundaries( 0 );
    checkBoundaries( 1 );
  }

  public int[] getPosition() {
    return new int[] { ( int ) position[ 0 ], ( int ) position[ 1 ] };
  }

  public void setSectorMapSize(int sectorMapSize) {
    this.sectorMapSize = sectorMapSize;
  }

  public void setSectorBoundarySize(int sectorBoundarySize) {
    this.sectorBoundarySize = sectorBoundarySize;
  }

  public void updatePrimaryServer(TCPConnection connection) {
    synchronized (primaryServer) {
        primaryServer = connection;
        LOG.info("Updated Primary Server to: " + connection.getTCPSender().getDestination());
    }
  }

  /**
   * 
   * @return the sector the client is currently in
   */
  public Sector getInitialSector() {
    return sector;
  }

  /**
   * 
   * @param connection to the initial Server
   */
  public void setInitialServerConnection(TCPConnection connection) {
      primaryServer = connection;
  }

  /**
   * 
   * @throws IOException
   */
  public void deliver() throws IOException {
    int[] pos = new int[] { ( int ) position[ 0 ], ( int ) position[ 1 ] };

    Set<Sector> contributions = getSectorContributions( pos );
    LOG.info( String.format( "Position: %d,%d, Sector: %d,%d", pos[ 0 ], pos[ 1 ] , sector.x, sector.y) );
    synchronized (primaryServer) {
      primaryServer.getTCPSender()
              .sendData(new SectorWindowRequest(Protocol.SECTOR_WINDOW_REQUEST,
                      Instant.now().toEpochMilli(), contributions, sector,
                      Properties.SECTOR_WINDOW_SIZE, pos, contributions.size(), port)
                      .getBytes());
    }
  }

  /**
   * @param pos of the client at time <t>T</t>
   * 
   */
  public Set<Sector> getSectorContributions(int[] pos) {
    Set<Sector> sectors = new HashSet<>();
    sectors.add( sector );

    boolean north = false, east = false, south = false, west = false;

    // North
    if ( sector.getX() != 0
        && position[ 0 ] - Properties.SECTOR_WINDOW_SIZE < 0 )
    {
      north = true;
      sectors.add( new Sector( sector.x - 1, sector.y ) );
    }

    // East
    if ( sector.getY() != sectorMapSize - 1 && position[ 1 ]
        + Properties.SECTOR_WINDOW_SIZE > sectorBoundarySize - 1 )
    {
      east = true;
      sectors.add( new Sector( sector.x, sector.y + 1 ) );
    }

    // South
    if ( sector.getX() != sectorMapSize - 1 && position[ 0 ]
        + Properties.SECTOR_WINDOW_SIZE > sectorBoundarySize - 1 )
    {
      south = true;
      sectors.add( new Sector( sector.x + 1, sector.y ) );
    }

    // West
    if ( sector.getY() != 0
        && position[ 1 ] - Properties.SECTOR_WINDOW_SIZE < 0 )
    {
      west = true;
      sectors.add( new Sector( sector.x, sector.y - 1 ) );
    }

    // Diagonals
    if ( north && east )
    {
      sectors.add( new Sector( sector.x - 1, sector.y + 1 ) );
    }
    if ( south && east )
    {
      sectors.add( new Sector( sector.x + 1, sector.y + 1 ) );
    }
    if ( south && west )
    {
      sectors.add( new Sector( sector.x + 1, sector.y - 1 ) );
    }
    if ( north && west )
    {
      sectors.add( new Sector( sector.x - 1, sector.y - 1 ) );
    }

    return sectors;
  }

  /**
   * Ensure the client is not going out of bounds, resetting the client
   * to the appropriate position and a zero velocity.
   * 
   * @param cord
   */
  public void checkBoundaries(int cord) {
    int sectorLocation;
    if ( cord == 0 )
    {
      sectorLocation = sector.getX();
    } else
    {
      sectorLocation = sector.getY();
    }

    // North // West
    if ( sectorLocation == 0 )
    {
      if ( position[ cord ] < Properties.SECTOR_WINDOW_SIZE )
      {
        position[ cord ] = Properties.SECTOR_WINDOW_SIZE;
        if ( velocity[ cord ] < 0 )
        {
          velocity[ cord ] = -velocity[ cord ];
        }
      }
    }

    // South // East
    if ( sectorLocation == sectorMapSize - 1 )
    {
      if ( position[ cord ] > sectorBoundarySize
          - Properties.SECTOR_WINDOW_SIZE )
      {
        position[ cord ] = sectorBoundarySize - Properties.SECTOR_WINDOW_SIZE;
        if ( velocity[ cord ] > 0 )
        {
          velocity[ cord ] = -velocity[ cord ];
        }
      }
    }

    int temp = sectorLocation;

    if ( position[ cord ] < 0 )
    {
      position[ cord ] = sectorBoundarySize + position[ cord ] - 1;
      --temp;
    } else if ( position[ cord ] > sectorBoundarySize - 1 )
    {
      position[ cord ] -= sectorBoundarySize;
      ++temp;
    }
    if ( cord == 0 )
    {
      sector.setX( temp );
    } else
    {
      sector.setY( temp );
    }

  }

  /**
   * Initialize the client to start traversing the environment of the
   * specified by it's location
   * 
   */
  public void run() {

    checkBoundaries( 0 );
    checkBoundaries( 1 );

    double deltaT = 0.2; // Euler integration time step

    int[] actions = new int[] { -1, 0, 1 };
    int r = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];
    int c = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];

    int count = 0;
    try
    {
      while ( !Thread.currentThread().isInterrupted() )
      {

        if ( ++count > 25 )
        {
          count = 0;
          r = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];
          c = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];
        }

        // Update position
        position[ 0 ] += deltaT * velocity[ 0 ];
        position[ 1 ] += deltaT * velocity[ 1 ];

        // Update velocity. Includes friction.
        velocity[ 0 ] += deltaT * ( 2 * r - 0.2 * velocity[ 0 ] );
        velocity[ 1 ] += deltaT * ( 2 * c - 0.2 * velocity[ 1 ] );

        checkBoundaries( 0 );
        checkBoundaries( 1 );
        
        deliver();
        
        TimeUnit.MILLISECONDS.sleep( 100 );
      }
    } catch ( InterruptedException | IOException e )
    {
      LOG.error( "Client stopped moving. " + e.toString() );
      e.printStackTrace();
    }

  }

  @Override
  public String toString() {
    return "sector: (" + sector.toString() + ") & position: "
        + Arrays.toString( position );
  }
}
