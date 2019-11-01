package distributed.application.heartbeat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.TimerTask;
import distributed.application.metadata.ServerMetadata;
import distributed.application.util.Properties;
import distributed.application.wireformats.ApplicationHeartbeat;
import distributed.common.transport.TCPConnection;
import distributed.common.util.Logger;
import distributed.common.wireformats.Protocol;

/**
 * A heartbeat message from the server to the switch.
 * 
 * The server heartbeat messages are setup on timed intervals, to send
 * a both minor and major heartbeats.
 * 
 * @author stock
 *
 */
public class ServerHeartbeatManager extends TimerTask {

  private final static Logger LOG =
      Logger.getInstance( Properties.SYSTEM_LOG_LEVEL );

  private final TCPConnection switchConnection;

  private final ServerMetadata metadata;

  private final ApplicationHeartbeat heartbeat;

  /**
   * Default constructor -
   * 
   * @param switchConnection
   * @param metadata
   */
  public ServerHeartbeatManager(TCPConnection switchConnection,
      ServerMetadata metadata) {
    this.switchConnection = switchConnection;
    this.metadata = metadata;
    this.heartbeat = new ApplicationHeartbeat( Protocol.APPLICATION_HEATBEAT,
        metadata.getIdentifier() );
  }

  @Override
  public void run() {
    try
    {
      heartbeat.setThreadCount(
          ManagementFactory.getThreadMXBean().getThreadCount() );
      heartbeat.setSectorIdentifiers( metadata.getSectorIdentifiers() );

      switchConnection.getTCPSender().sendData( heartbeat.getBytes() );
    } catch ( IOException e )
    {
      LOG.error(
          "Unable to send heartbeat message to switch. " + e.getMessage() );
      e.printStackTrace();
    }
  }
}
