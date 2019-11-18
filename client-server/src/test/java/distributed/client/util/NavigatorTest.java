package distributed.client.util;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import distributed.common.util.Sector;

public class NavigatorTest {

  @Test
  public void testGetSectorContributions() {
    int port = 42134;
    Sector initial = new Sector( 0, 0 );
    Navigator n = new Navigator( initial, new int[] { 5000, 5000 }, port );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    Set<Sector> expected = new HashSet<>();
    expected.add( initial );

    System.out.println( "position: " + Arrays.toString( n.getPosition() )
        + ", sector: " + initial );

    Set<Sector> actual = n.getSectorContributions( n.getPosition() );
    System.out.print( "Expected: " );
    actual.forEach( v -> System.out.print( v + ", " ) );

    System.out.print( "\nActual: " );
    actual.forEach( v -> System.out.print( v + ", " ) );
    System.out.println();
    System.out.println();

    assertEquals( expected, actual );

    initial = new Sector( 0, 0 );
    n = new Navigator( initial, new int[] { 0, 5000 }, port );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    expected = new HashSet<>();
    expected.add( initial );
    expected.add( new Sector( 3, 0 ) );

    System.out.println( "position: " + Arrays.toString( n.getPosition() )
        + ", sector: " + initial );

    actual = n.getSectorContributions( n.getPosition() );
    System.out.print( "Expected: " );
    actual.forEach( v -> System.out.print( v + ", " ) );

    System.out.print( "\nActual: " );
    actual.forEach( v -> System.out.print( v + ", " ) );
    System.out.println();
    System.out.println();

    assertEquals( expected, actual );

    initial = new Sector( 0, 0 );
    n = new Navigator( initial, new int[] { 9999, 9999 }, port );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    expected = new HashSet<>();
    expected.add( initial );
    expected.add( new Sector( 1, 0 ) );
    expected.add( new Sector( 0, 1 ) );
    expected.add( new Sector( 1, 1 ) );

    System.out.println( "position: " + Arrays.toString( n.getPosition() )
        + ", sector: " + initial );

    actual = n.getSectorContributions( n.getPosition() );
    System.out.print( "Expected: " );
    actual.forEach( v -> System.out.print( v + ", " ) );

    System.out.print( "\nActual: " );
    actual.forEach( v -> System.out.print( v + ", " ) );
    System.out.println();
    System.out.println();

    assertEquals( expected, actual );

    initial = new Sector( 0, 0 );
    n = new Navigator( initial, new int[] { 0, 0 }, port );
    n.setSectorBoundarySize( 10000 );
    n.setSectorMapSize( 4 );

    expected = new HashSet<>();
    expected.add( initial );
    expected.add( new Sector( 3, 0 ) );
    expected.add( new Sector( 0, 3 ) );
    expected.add( new Sector( 3, 3 ) );

    System.out.println( "position: " + Arrays.toString( n.getPosition() )
        + ", sector: " + initial );

    actual = n.getSectorContributions( n.getPosition() );
    System.out.print( "Expected: " );
    actual.forEach( v -> System.out.print( v + ", " ) );

    System.out.print( "\nActual: " );
    actual.forEach( v -> System.out.print( v + ", " ) );
    System.out.println();
    System.out.println();

    assertEquals( expected, actual );
  }

}
