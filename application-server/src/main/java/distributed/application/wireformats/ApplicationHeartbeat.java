package distributed.application.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import distributed.common.wireformats.Event;
import distributed.common.wireformats.Protocol;

/**
 * 
 * @author stock
 *
 */
public class ApplicationHeartbeat implements Event {

  private int type;

  private String identifier;

  private int threadCount;

  /**
   * Constructor with assigned message
   * 
   */
  public ApplicationHeartbeat(int type, String identifier) {
    this.type = type;
    this.identifier = identifier;
  }

  /**
   * Constructor - Unmarshall the <code>byte[]</code> to the respective
   * class elements.
   * 
   * @param marshalledBytes is the byte array of the class.
   * @throws IOException
   */
  public ApplicationHeartbeat(byte[] marshalledBytes) throws IOException {
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream( marshalledBytes );
    DataInputStream din =
        new DataInputStream( new BufferedInputStream( inputStream ) );

    this.type = din.readInt();

    int len = din.readInt();
    byte[] msg = new byte[ len ];
    din.readFully( msg );
    this.identifier = new String( msg );

    this.threadCount = din.readInt();

    inputStream.close();
    din.close();
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
  public String getIdentifier() {
    return identifier;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public void setThreadCount(int threadCount) {
    this.threadCount = threadCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DataOutputStream dout =
        new DataOutputStream( new BufferedOutputStream( outputStream ) );

    dout.writeInt( type );

    byte[] msg = identifier.getBytes();
    dout.writeInt( msg.length );
    dout.write( msg );

    dout.writeInt( threadCount );

    dout.flush();
    marshalledBytes = outputStream.toByteArray();

    outputStream.close();
    dout.close();
    return marshalledBytes;
  }

  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }

}
