package cs555.system.util;

/**
 * Class used to print <tt>info</tt>, <tt>debug</tt> and
 * <tt>error</tt> logs to the console.
 * 
 * Initialize a new Logger for a give class. Use as a private final
 * static object in the calling class.
 * 
 * @author stock
 *
 */
public class Logger {

  private final static Logger instance = new Logger();

  private final boolean INFO;

  private final boolean DEBUG;

  /**
   * Private constructor
   * 
   */
  private Logger() {
    if ( cs555.system.util.Properties.SYSTEM_LOG_LEVEL
        .equalsIgnoreCase( "INFO" ) )
    {
      this.INFO = true;
      this.DEBUG = false;
    } else
    {
      this.INFO = true;
      this.DEBUG = true;
    }
  }

  /**
   * Single instance ensures that singleton instances are created only
   * when needed.
   * 
   * @return Returns the instance for the class
   */
  public static Logger getInstance() {
    return instance;
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
   * Retrieve the details for the log.
   * 
   * @return Return a <code>String</code> of the caller class in the
   *         format: <code>caller(method:line)</code>.
   */
  private StringBuilder details() {
    String line = Integer.toString(
        Thread.currentThread().getStackTrace()[ 3 ].getLineNumber() );

    String method = Thread.currentThread().getStackTrace()[ 3 ].getMethodName();

    String caller = Thread.currentThread().getStackTrace()[ 3 ].getClassName();

    return ( new StringBuilder() ).append( caller ).append( "(" )
        .append( method ).append( ":" ).append( line ).append( ") " );
  }

  /**
   * Display the message with details for the <b>'INFO'</b> type.
   * Configured by the global {@link Logger#INFO}.
   * 
   * @param message The message to display
   */
  public void info(String message) {
    if ( INFO )
    {
      System.out.println(
          details().append( "[INFO] - " ).append( message ).toString() );
    }
  }

  /**
   * Display the message with details for the <b>'DEBUG'</b> type.
   * Configured by the global {@link Logger#DEBUG}.
   * 
   * @param message The message to display
   */
  public void debug(String message) {
    if ( DEBUG )
    {
      System.out.println(
          details().append( "[DEBUG] - " ).append( message ).toString() );
    }
  }

  /**
   * Display the message with details for the <b>'ERROR'</b> type.
   * 
   * @param message The message to display
   */
  public void error(String message) {
    System.out.println(
        details().append( "[ERROR] - " ).append( message ).toString() );
  }

}
