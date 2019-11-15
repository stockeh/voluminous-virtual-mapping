package distributed.application.metadata;

public class SectorInformation {

  private final byte[][] sector;

  public SectorInformation(byte[][] sector) {
    this.sector = sector;
  }
  
  public final byte[][] getSector() {
    return sector;
  }
}
