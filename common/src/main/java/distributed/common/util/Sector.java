package distributed.common.util;

import java.util.Objects;

public class Sector {

  public int x;

  public int y;

  public Sector() {
    this.x = 0;
    this.y = 0;
  }

  public Sector(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void update(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return x + "," + y;
  }

  @Override
  public int hashCode() {
    return Objects.hash( x, y );
  }

  @Override
  public boolean equals(Object o) {
    if ( !( o instanceof Sector ) )
    {
      return false;
    }
    Sector sector = ( Sector ) o;
    if ( x == sector.x && y == sector.y )
    {
      return true;
    }
    return false;
  }
}
