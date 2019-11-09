package distributed.common.wireformats;

import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;

import java.io.IOException;

/**
 * 
 * @author stock
 *
 */
public class GenericMessage implements Event {

  public int type;

  public String message;


  /**
   * Constructor without assigned message
   * 
   */
  public GenericMessage(int type) {
    this.type = type;
    this.message = "";
  }
  
  /**
   * Constructor with assigned message
   * 
   */
  public GenericMessage(int type, String message) {
    this.type = type;
    this.message = message;
  }

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public GenericMessage(byte[] marshalledBytes) throws IOException {
    MessageUnMarshaller.readEvent( getClass(), this, marshalledBytes );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getType() {
    return type;
  }

  /**
   * 
   * @return
   */
  public String getMessage() {
    return message;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getBytes() throws IOException {
    return MessageMarshaller.writeEvent( getClass(), this );
  }

  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }

}
