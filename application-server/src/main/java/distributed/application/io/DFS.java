package distributed.application.io;

import distributed.application.util.Properties;
import distributed.common.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DFS {
  private static Configuration configuration;
  private static final Logger LOG =
          Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );
  private static final SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

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

    Date date = new Date(System.currentTimeMillis());
    LOG.info("Reading file: " + filename + " with size " + size + " TIME: " + formatter.format(date));
    byte[] fileBytes = new byte[size];
    dataInputStream.readFully(fileBytes);
    LOG.info("Finished reading file: " + filename + " TIME: " + formatter.format(date));
    return fileBytes;
  }


}
