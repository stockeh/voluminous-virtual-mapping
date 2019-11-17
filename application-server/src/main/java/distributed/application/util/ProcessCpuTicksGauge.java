package distributed.application.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import com.codahale.metrics.Gauge;

@SuppressWarnings( "restriction" )
public class ProcessCpuTicksGauge implements Gauge<Double> {

  private final OperatingSystemMXBean osMxBean;

  public ProcessCpuTicksGauge() throws ClassNotFoundException, IOException {

    osMxBean = ManagementFactory.getPlatformMXBean( OperatingSystemMXBean.class );

  }

  @Override
  public Double getValue() {
    return osMxBean.getSystemCpuLoad();
  }
}
