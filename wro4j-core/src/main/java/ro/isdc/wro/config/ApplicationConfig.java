/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.config;

/**
 * Defines MBean which manage configuration.
 * @author Alex Objelean
 */
public class ApplicationConfig
  implements ApplicationConfigMBean {
  private long cacheUpdatePeriod;
  private long modelUpdatePeriod;
  /**
   * @return the name of the object used to register the MBean.
   */
  public static String getObjectName() {
    return ApplicationConfig.class.getPackage().getName() + ".jmx:type=" + ApplicationConfig.class.getSimpleName();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized long getCacheUpdatePeriod() {
    return this.cacheUpdatePeriod;
  }


  /**
   * {@inheritDoc}
   */
  public synchronized long getModelUpdatePeriod() {
    return modelUpdatePeriod;
  }


  /**
   * {@inheritDoc}
   */
  public synchronized void setCacheUpdatePeriod(final long period) {
    this.cacheUpdatePeriod = period;
  }


  /**
   * {@inheritDoc}
   */
  public synchronized void setModelUpdatePeriod(final long period) {
    this.modelUpdatePeriod = period;
  }
}
