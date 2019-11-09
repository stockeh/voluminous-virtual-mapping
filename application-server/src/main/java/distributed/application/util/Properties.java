package distributed.application.util;

import distributed.common.util.Configurations;
import org.apache.kerby.config.Conf;

/**
 * 
 * @author stock
 *
 */
public interface Properties {

  final String CONF_NAME = "application.properties";

  final String SWITCH_HOST =
      Configurations.getInstance(CONF_NAME).getProperty( "switch.host" );

  final int SWITCH_PORT = Integer
      .parseInt( Configurations.getInstance(CONF_NAME).getProperty( "switch.port" ) );

  final String SYSTEM_LOG_LEVEL =
      Configurations.getInstance(CONF_NAME).getProperty( "system.log.level", "INFO" );

  final String CORE_SITE_LOCATION= Configurations.getInstance(CONF_NAME).getProperty("core.site.conf.location");

  final String HDFS_SITE_LOCATION= Configurations.getInstance(CONF_NAME).getProperty("hdfs.site.conf.location");

  final String HDFS_FILE_LOCATION= Configurations.getInstance(CONF_NAME).getProperty("hdfs.file.location");

  final int SECTOR_BOUNDARY_SIZE = Integer.parseInt(Configurations.getInstance(CONF_NAME).getProperty("sector.boundary.size"));
}
