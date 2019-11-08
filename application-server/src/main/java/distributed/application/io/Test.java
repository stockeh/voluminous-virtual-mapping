package distributed.application.io;

import java.io.IOException;

public class Test {
  public static void main(String[] args) throws IOException {
    DFS.readFile("/transport/delays/part-r-00000");
  }
}
