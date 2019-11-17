package distributed.client.util;

import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import distributed.common.util.Sector;

public class NavigatorTest {

  @Test
  public void testGetSectorContributions() {

    Sector initial = new Sector( 0, 0 );
    Navigator n = new Navigator( initial, new int[] { 5000, 5000 } );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    Set<Sector> expected = new HashSet<>();
    expected.add( initial );

    assertEquals( expected, n.getSectorContributions( n.getPosition() ) );

    initial = new Sector( 0, 0 );
    n = new Navigator( initial, new int[] { 0, 5000 } );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    expected = new HashSet<>();
    expected.add( initial );
    expected.add( new Sector( 3, 0 ) );

    assertEquals( expected, n.getSectorContributions( n.getPosition() ) );

    initial = new Sector( 0, 0 );
    n = new Navigator( initial, new int[] { 9999, 9999 } );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    expected = new HashSet<>();
    expected.add( initial );
    expected.add( new Sector( 1, 0 ) );
    expected.add( new Sector( 0, 1 ) );
    expected.add( new Sector( 1, 1 ) );

    assertEquals( expected, n.getSectorContributions( n.getPosition() ) );
    
    initial = new Sector( 0, 0 );
    n = new Navigator( initial, new int[] { 0, 0 } );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    expected = new HashSet<>();
    expected.add( initial );
    expected.add( new Sector( 3, 0 ) );
    expected.add( new Sector( 0, 3 ) );
    expected.add( new Sector( 3, 3 ) );

    assertEquals( expected, n.getSectorContributions( n.getPosition() ) );
  }

}
