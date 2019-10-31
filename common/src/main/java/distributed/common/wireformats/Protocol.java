package distributed.common.wireformats;

/**
 * Interface defining the wireformats between nodes.
 *
 * @author stock
 *
 */
public interface Protocol {

  int DISCOVER_REQUEST = 0;
  int DISCOVER_RESPONSE = 1;
  int REGISTER_SERVER_REQUEST = 2;
  int REGISTER_SERVER_RESPONSE = 3;

}
