package distributed.application.io;

import distributed.application.util.Properties;
import distributed.common.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class DFS {
  private static Configuration configuration;
  private static final Logger LOG =
          Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  static {
    configuration = new Configuration();
    configuration.addResource(new Path(Properties.CORE_SITE_LOCATION));
    configuration.addResource(new Path(Properties.HDFS_SITE_LOCATION));
  }

  public static byte[] readFile(String filename) throws IOException {
    Path path = new Path(filename);
    FileSystem fileSystem = path.getFileSystem(configuration);
    FSDataInputStream dataInputStream = fileSystem.open(path);

    int size = dataInputStream.available();
    LOG.info("Reading file: " + filename + " with size" + size);
    byte[] fileBytes = new byte[size];
    dataInputStream.readFully(fileBytes);
    return fileBytes;
  }
}
