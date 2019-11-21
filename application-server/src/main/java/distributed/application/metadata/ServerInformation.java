package distributed.application.metadata;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Sector;

/**
 * 
 * @author stock
 *
 */
public class ServerInformation {

  private final TCPConnection connection;

  private int threadCount;

  private Set<Sector> sectorIdentifiers = new HashSet<>();

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

  public Set<Sector> getSectorIdentifiers() {
    return sectorIdentifiers;
  }

  public void setSectorIdentifiers(Set<Sector> sectorIdentifiers) {
    this.sectorIdentifiers = sectorIdentifiers;
  }

  public int getNumSectors() { return this.sectorIdentifiers.size(); }

  public int getRandomComparable() { return ThreadLocalRandom.current().nextInt(); }

  public String toString() {
    return connection.getTCPSender().getDestination() + " SECTORS: " + sectorIdentifiers.stream()
            .map(Sector::toString).collect(Collectors.joining(","));
  }
}
