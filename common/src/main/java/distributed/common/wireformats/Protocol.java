package distributed.common.wireformats;

/**
 * Interface defining the wireformats between nodes.
 *
 * @author stock
 *
 */
public interface Protocol {

  int CLIENT_DISCOVER_REQUEST = 0;

  int CLIENT_DISCOVER_RESPONSE = 1;

  int REGISTER_SERVER_REQUEST = 2;

  int REGISTER_SERVER_RESPONSE = 3;

  int APPLICATION_HEATBEAT = 4;
  
  int GET_SECTOR_REQUEST = 5;

  int SECTOR_WINDOW_REQUEST = 6;
  
  int SECTOR_WINDOW_RESPONSE = 7;

  int SERVER_INITIALIZED = 8;

  int SECTOR_LOADED = 9;

  int PREFETCH_SECTORS = 10;

  int LOAD_SECTOR = 11;
}
