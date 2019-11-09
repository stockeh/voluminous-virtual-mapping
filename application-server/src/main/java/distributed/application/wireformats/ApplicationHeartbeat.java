package distributed.application.wireformats;

import java.io.IOException;
import java.util.Set;

import distributed.common.util.Sector;
import distributed.common.wireformats.Event;
import distributed.common.util.MessageMarshaller;
import distributed.common.util.MessageUnMarshaller;
import distributed.common.wireformats.Protocol;

/**
 * 
 * @author stock
 *
 */
public class ApplicationHeartbeat implements Event {

  public int type;

  public String identifier;

  public int threadCount;
  
  public Set<Sector> sectorIdentifiers;

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
    MessageUnMarshaller.readEvent(getClass(), this, marshalledBytes);
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
  
  public Set<Sector> getSectorIdentifiers() {
    return sectorIdentifiers;
  }

  public void setSectorIdentifiers(Set<Sector> sectorIdentifiers) {
    this.sectorIdentifiers = sectorIdentifiers;
  }


  @Override
  public byte[] getBytes() throws IOException {
    return MessageMarshaller.writeEvent(getClass(), this);
  }

  @Override
  public String toString() {
    return Protocol.class.getFields()[ type ].getName().toString();
  }

}
