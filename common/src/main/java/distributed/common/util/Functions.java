package distributed.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Functions {
  public static void deleteDirectory(Path directory) {
    if(!Files.exists(directory)) return;
    try {
      Files.walk(directory)
              .sorted(Comparator.reverseOrder())
              .map(Path::toFile)
              .forEach(File::delete);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
