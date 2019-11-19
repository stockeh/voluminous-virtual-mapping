package distributed.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Functions {

  private static final Logger LOG = Logger.getInstance( "DEBUG" );

  public static void deleteDirectory(Path directory) {
    if ( !Files.exists( directory ) )
      return;
    try
    {
      Files.walk( directory ).sorted( Comparator.reverseOrder() )
          .map( Path::toFile ).forEach( File::delete );
    } catch ( IOException e )
    {
      LOG.error( "Unable to recursively delete files in each directory. "
          + e.toString() );
      e.printStackTrace();
    }
  }

}
