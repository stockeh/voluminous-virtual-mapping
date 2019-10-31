package distributed.common.wireformats;

import java.io.IOException;


public class GenericPortMessage implements Event{

  public int type;
  public int port;

  public GenericPortMessage(int type, int port) {
    this.type = type;
    this.port = port;
  }

  public GenericPortMessage(byte[] bytes) {
    MessageReader messageReader = new MessageReader(bytes);
    messageReader.readEvent(getClass(), this);
  }

  @Override
  public int getType() {
    return type;
  }

  @Override
  public byte[] getBytes() throws IOException {
    MessageUnMarshaller messageMarshaller = new MessageUnMarshaller();
    messageMarshaller.writeEvent(getClass(), this);
    return messageMarshaller.getMarshalledData();
  }
}
