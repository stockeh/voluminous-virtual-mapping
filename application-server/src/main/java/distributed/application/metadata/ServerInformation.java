package distributed.application.metadata;

import java.util.HashSet;
import java.util.Set;
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

  private Set<String> sectorIdentifiers = new HashSet<>();

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
    this.sectorIdentifiers = message.sectorIdentifiers;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public void setThreadCount(int threadCount) {
    this.threadCount = threadCount;
  }

  public TCPConnection getConnection() {
    return connection;
  }

  public Set<String> getSectorIdentifiers() {
    return sectorIdentifiers;
  }

  public void setSectorIdentifiers(Set<String> sectorIdentifiers) {
    this.sectorIdentifiers = sectorIdentifiers;
  }
}
