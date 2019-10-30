package cs555.system.util;

import java.util.Properties;

/**
 * Singleton class to load properties file for configuration.
 * 
 * @author stock
 *
 */
public final class Configurations {

  private final Properties properties;

  private final static Configurations instance = new Configurations();

  /**
   * Private constructor
   * 
   */
  private Configurations() {
    this.properties = new Properties();
    try
    {
      properties.load( getClass().getClassLoader()
          .getResourceAsStream( cs555.system.util.Properties.CONF_NAME ) );
    } catch ( Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * Override the clone method to ensure the "unique instance"
   * requirement of this class.
   * 
   */
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  /**
   * Get the properties instance using a singleton pattern to guarantee
   * the creation of only one instance.
   * 
   * @return the instance associated with the properties file - creates
   *         a new instance if not previously created.
   */
  public static Configurations getInstance() {
    return instance;
  }

  /**
   * 
   * @param key to search properties for
   * @return a property of the property file denoted by the key
   */
  public String getProperty(String key) {
    String result = null;
    if ( key != null && !key.trim().isEmpty() )
    {
      result = this.properties.getProperty( key );
    }
    return result;
  }

  /**
   * 
   * @param key to search properties for
   * @param default value
   * @return a property of the property file denoted by the key
   */
  public String getProperty(String key, String defaultValue) {
    String result = null;
    if ( key != null && !key.trim().isEmpty() )
    {
      result = this.properties.getProperty( key, defaultValue );
    }
    return result;
  }
}
