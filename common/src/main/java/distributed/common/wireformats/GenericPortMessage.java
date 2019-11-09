package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;

import java.io.IOException;


public class GenericPortMessage implements Event {

  public int type;
  public int port;

  public GenericPortMessage(int type, int port) {
    this.type = type;
    this.port = port;
  }

  public GenericPortMessage(byte[] bytes) {
    MessageUnMarshaller.readEvent( getClass(), this, bytes );
  }

  @Override
  public int getType() {
    return type;
  }

  /**
   * 
   * @return the port added to the message
   */
  public int getPort() {
    return port;
  }

  @Override
  public byte[] getBytes() throws IOException {
    return MessageMarshaller.writeEvent( getClass(), this );
  }

  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }
}
