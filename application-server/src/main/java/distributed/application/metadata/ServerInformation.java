package distributed.application.metadata;

import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;

/**
 * 
 * @author stock
 *
 */
public class ServerInformation {

  private final TCPConnection connection;

  private int threadCount;

  /**
   * Set final server information for newly connected server.
   * 
   * @param connection
   */
  public ServerInformation(TCPConnection connection) {
    this.connection = connection;
  }

  public void updateServerInformation(ApplicationHeartbeat message) {
    this.threadCount = message.getThreadCount();
  }


}
