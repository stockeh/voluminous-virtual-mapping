package distributed.client.util;

import distributed.common.util.Configurations;

/**
 * 
 * @author stock
 *
 */
public interface Properties {

  final String CONF_NAME = "client.properties";

  final String SWITCH_HOST =
      Configurations.getInstance( CONF_NAME ).getProperty( "switch.host" );

  final int SWITCH_PORT = Integer.parseInt(
      Configurations.getInstance( CONF_NAME ).getProperty( "switch.port" ) );

  final int SECTOR_WINDOW_SIZE = Integer.parseInt( Configurations
      .getInstance( CONF_NAME ).getProperty( "switch.port", "10" ) );

  final String SYSTEM_LOG_LEVEL = Configurations.getInstance( CONF_NAME )
      .getProperty( "system.log.level", "INFO" );

}
