package distributed.common.util;

/**
 * 
 * @author stock
 *
 */
public interface Properties {

  final String CONF_NAME = "application.properties";

  final String SWITCH_HOST =
      Configurations.getInstance().getProperty( "switch.host" );

  final int SWITCH_PORT = Integer
      .parseInt( Configurations.getInstance().getProperty( "switch.port" ) );

  final String SYSTEM_LOG_LEVEL =
      Configurations.getInstance().getProperty( "system.log.level", "INFO" );

}
