package distributed.common.util;

import distributed.common.wireformats.MessageMarshaller;
import distributed.common.wireformats.MessageUnMarshaller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Sector {
  private int x;
  private int y;

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Sector(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Sector(byte[] bytes) {
    MessageUnMarshaller.readEvent(this.getClass(), this, bytes);
  }

  public String toString() {
    return x+ "," + y;
  }


  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  public void writeSector() throws IOException {
    MessageMarshaller.writeEvent(this.getClass(), this);
  }

  @Override
  public boolean equals(Object o) {
    if(!(o instanceof System)) {
      return false;
    }
    Sector sector = (Sector) o;
    if(x == sector.x && y == sector.y) {
      return true;
    }
    return false;
  }
}
