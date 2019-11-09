package distributed.application.io;

import distributed.common.util.Sector;

import java.io.IOException;

public class Test {
  public static void main(String[] args) throws IOException {
    Sector sector = new Sector(10, 1);
    Sector sector1 = new Sector(10, 1);

    System.out.println(sector.hashCode());
    System.out.println(sector1.hashCode());
  }
}
