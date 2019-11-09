package distributed.application.metadata;

public class SectorInformation {

  private byte[][] sector;

  public SectorInformation(byte[][] bytes) {
    sector = bytes;
  }

  public final byte[][] getSector() { return sector; }

}
