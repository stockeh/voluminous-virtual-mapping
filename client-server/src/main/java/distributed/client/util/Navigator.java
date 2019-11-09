package distributed.client.util;

import java.io.IOException;
import java.time.Instant;
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
public class Navigator {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private int sectorBoundarySize;

  private int sectorMapSize;

  private TCPConnection primaryServer;

  private Sector sector;

  private double[] position;

  private double[] velocity;

  /**
   * 
   * @param initialSector
   * @param initialPosition
   */
  public Navigator(Sector initialSector, int[] initialPosition) {
    this.sector = initialSector;

    position = new double[] { initialPosition[ 0 ], initialPosition[ 1 ] };

    velocity = new double[] { 0, 0 };
  }

  public void setSectorMapSize(int sectorMapSize) {
    this.sectorMapSize = sectorMapSize;
  }

  public void setSectorBoundarySize(int sectorBoundarySize) {
    this.sectorBoundarySize = sectorBoundarySize;
  }

  /**
   * 
   * @return the sector that was originally specified by the client
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

    primaryServer.getTCPSender()
        .sendData( new SectorWindowRequest( Protocol.SECTOR_WINDOW_REQUEST,
            Instant.now().toEpochMilli(), getSectorContributions( pos ), sector,
            Properties.SECTOR_WINDOW_SIZE, pos ).getBytes() );
  }

  /**
   * @param pos
   * 
   * 
   */
  public Set<Sector> getSectorContributions(int[] pos) {
    Set<Sector> sectors = new HashSet<>();


    sectors.add( sector );
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
  public void init() {

    double deltaT = 0.1; // Euler integration time step

    int[] actions = new int[] { -1, 0, 1 };
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
        TimeUnit.MILLISECONDS.sleep( 250 );
      }
    } catch ( InterruptedException | IOException e )
    {
      LOG.error( "Client stopped moving. " + e.toString() );
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    Navigator n = new Navigator( new Sector(), new int[] { 0, 0 } );
    n.init();
  }

}