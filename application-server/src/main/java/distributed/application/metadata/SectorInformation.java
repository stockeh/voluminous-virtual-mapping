package distributed.application.metadata;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SectorInformation {

  private byte[][] sector;

  protected final ConditionalLock conditionalLock;

  public SectorInformation() {
    conditionalLock = new ConditionalLock();
  }

  public void setSector(byte[][] sector)
  {
    this.sector = sector;
  }
  
  public final byte[][] getSector() {
    return sector;
  }

  public ConditionalLock getConditionalLock() {
    return conditionalLock;
  }

  /**
   * 
   * @author stock
   *
   */
  public class ConditionalLock {

    private final Lock lock;

    private final Condition condition;

    private boolean initialized;

    public ConditionalLock() {
      this.lock = new ReentrantLock();
      this.condition = lock.newCondition();
      this.initialized = false;
    }

    /**
     * 
     */
    public void initialized() {
      initialized = true;
      try
      {
        lock.lock();
        condition.signal();
      } finally
      {
        lock.unlock();
      }
    }

    /**
     * 
     * @return the {@code ReentrantLock} for the {@code Condition}
     */
    public Lock getLock() {
      return lock;
    }

    /**
     * 
     * @return true if the sector has been loaded, false otherwise.
     */
    public boolean isInitialized() {
      return initialized;
    }

    /**
     * 
     * @return
     */
    public Condition getCondition() {
      return condition;
    }
  }

}
