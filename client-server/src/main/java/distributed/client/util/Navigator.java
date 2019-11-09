package distributed.client.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Logger;

/**
 * 
 * @author stock
 *
 */
public class Navigator {

  private static final Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  // server identifiers, connection
  private final Map<String, TCPConnection> servers;

  // TODO: Load this value from the request of the switch
  private int sectorBoundarySize = 100;

  // TODO: Load this value from the request of the switch
  private int sectorMapSize = 4;

  private String sectorIdentifier;

  private int[] sector;

  private double[] position;

  private double[] velocity;

  /**
   * 
   * @param initialSector
   * @param initialLocation
   */
  public Navigator(String initialSector, String initialLocation) {
    this.sectorIdentifier = initialSector;
    this.servers = new HashMap<>();

    String[] temp = initialSector.split( "," );
    sector = new int[] { Integer.parseInt( temp[ 0 ] ),
        Integer.parseInt( temp[ 1 ] ) };

    temp = initialLocation.split( "," );
    position = new double[] { Double.parseDouble( temp[ 0 ] ),
        Double.parseDouble( temp[ 1 ] ) };

    velocity = new double[] { 0, 0 };
  }

  /**
   * 
   * @return the sector that was originally specified by the client
   */
  public String getInitialSector() {
    return sectorIdentifier;
  }

  /**
   * 
   * @param connection to the initial Server
   */
  public void setInitialServerConnection(TCPConnection connection) {
    servers.put( sectorIdentifier, connection );
  }

  /**
   * 
   * 
   */
  public void deliver() {

    int Xpos = ( int ) position[ 0 ];

    int Ypos = ( int ) position[ 1 ];
    
    if ( Xpos + Properties.SECTOR_WINDOW_SIZE > sectorBoundarySize )
    {
      
    }
  }

  /**
   * 
   * 
   */
  public void getSectorContributions() {
    
  }

  /**
   * Ensure the client is not going out of bounds, resetting the client
   * to the appropriate position and a zero velocity.
   * 
   * @param cord
   */
  public void checkBoundaries(int cord) {
    if ( position[ cord ] < 0 )
    {
      double n = sectorBoundarySize + position[ cord ];
      position[ cord ] =
          n > sectorBoundarySize - 1 ? sectorBoundarySize - 1 : n;

      if ( sector[ cord ] == 0 )
      {
        sector[ cord ] = sectorMapSize - 1;
      } else
      {
        sector[ cord ] -= 1;
      }

    } else if ( position[ cord ] > sectorBoundarySize - 1 )
    {
      double n = position[ cord ] - sectorBoundarySize - 1;
      position[ cord ] = n < 0 ? 0 : n;

      if ( sector[ cord ] == sectorMapSize - 1 )
      {
        sector[ cord ] = 0;
      } else
      {
        sector[ cord ] += 1;
      }
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
    } catch ( InterruptedException e )
    {
      LOG.error( "Client stopped moving. " + e.toString() );
      e.printStackTrace();
    }

  }

  public static void main(String[] args) {
    Navigator n = new Navigator( "0,0", "0,0" );
    n.init();
  }

}
