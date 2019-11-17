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

  private int sectorBoundarySize;

  private int sectorMapSize;

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
    LOG.info(String.format("Position: %d,%d", pos[0], pos[1]));
    primaryServer.getTCPSender()
        .sendData( new SectorWindowRequest( Protocol.SECTOR_WINDOW_REQUEST,
            Instant.now().toEpochMilli(), contributions, sector,
            Properties.SECTOR_WINDOW_SIZE, pos, contributions.size(), port ).getBytes() );
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
    if ( position[ 1 ] + Properties.SECTOR_WINDOW_SIZE > sectorBoundarySize
        - 1 )
    {
      north = true;
      sectors.add( new Sector( sector.x,
          Math.floorMod( sector.y + 1, sectorMapSize ) ) );
    }

    // East
    if ( position[ 0 ] + Properties.SECTOR_WINDOW_SIZE > sectorBoundarySize
        - 1 )
    {
      east = true;
      sectors.add( new Sector( Math.floorMod( sector.x + 1, sectorMapSize ),
          sector.y ) );
    }

    // South
    if ( position[ 1 ] - Properties.SECTOR_WINDOW_SIZE < 0 )
    {
      south = true;
      sectors.add( new Sector( sector.x,
          Math.floorMod( sector.y - 1, sectorMapSize ) ) );
    }

    // West
    if ( position[ 0 ] - Properties.SECTOR_WINDOW_SIZE < 0 )
    {
      west = true;
      sectors.add( new Sector( Math.floorMod( sector.x - 1, sectorMapSize ),
          sector.y ) );
    }

    // Diagonals
    if ( north && east )
    {
      sectors.add( new Sector( Math.floorMod( sector.y + 1, sectorMapSize ),
          Math.floorMod( sector.y + 1, sectorMapSize ) ) );
    }
    if ( south && east )
    {
      sectors.add( new Sector( Math.floorMod( sector.y + 1, sectorMapSize ),
          Math.floorMod( sector.y - 1, sectorMapSize ) ) );
    }
    
    if ( south && west )
    {
      sectors.add( new Sector( Math.floorMod( sector.y - 1, sectorMapSize ),
          Math.floorMod( sector.y - 1, sectorMapSize ) ) );
    }

    if ( north && west )
    {
      sectors.add( new Sector( Math.floorMod( sector.y - 1, sectorMapSize ),
          Math.floorMod( sector.y + 1, sectorMapSize ) ) );
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

    int temp = sectorLocation;

    if ( position[ cord ] < 0 )
    {
      double n = sectorBoundarySize + position[ cord ];
      position[ cord ] =
          n > sectorBoundarySize - 1 ? sectorBoundarySize - 1 : n;

      if ( sectorLocation == 0 )
      {
        temp = sectorMapSize - 1;
      } else
      {
        temp -= 1;
      }

    } else if ( position[ cord ] > sectorBoundarySize - 1 )
    {
      double n = position[ cord ] - sectorBoundarySize - 1;
      position[ cord ] = n < 0 ? 0 : n;

      if ( sectorLocation == sectorMapSize - 1 )
      {
        temp = 0;
      } else
      {
        temp += 1;
      }
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

    double deltaT = 0.2; // Euler integration time step

    int[] actions = new int[] { -1, 0 , 1 };
    int x = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];
    int y = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];

    int count = 0;
    try
    {
      while ( !Thread.currentThread().isInterrupted() )
      {

        if ( ++count > 25 )
        {
          count = 0;
          x = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];
          y = actions[ ThreadLocalRandom.current().nextInt( actions.length ) ];
        }

        // Update position
        position[ 0 ] += deltaT * velocity[ 0 ];
        position[ 1 ] += deltaT * velocity[ 1 ];

        // Update velocity. Includes friction.
        velocity[ 0 ] += deltaT * ( 2 * x - 0.2 * velocity[ 0 ] );
        velocity[ 1 ] += deltaT * ( 2 * y - 0.2 * velocity[ 1 ] );

        checkBoundaries( 0 );
        checkBoundaries( 1 );

        deliver();
        TimeUnit.MILLISECONDS.sleep( 1000 );
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
