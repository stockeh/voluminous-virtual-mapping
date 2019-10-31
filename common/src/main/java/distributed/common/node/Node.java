package distributed.common.node;

import distributed.common.transport.TCPConnection;
import distributed.common.wireformats.Event;

/**
 * Interface for the server and switch, so underlying communication is
 * indistinguishable, i.e., Nodes send messages to Nodes.
 * 
 * @author stock
 *
 */
public interface Node {

  /**
   * Gives the ability for events to be triggered by incoming messages
   * on a given node.
   * 
   * @param event
   * @param connection
   */
  public void onEvent(Event event, TCPConnection connection);

}
