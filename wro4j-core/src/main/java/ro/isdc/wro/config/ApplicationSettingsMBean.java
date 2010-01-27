/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.config;

/**
 * This interface defines the MBean which manage the wro4j configuration.

 * @author Alex Objelean
 */
public interface ApplicationSettingsMBean {
  long getModelUpdatePeriod();
  void setModelUpdatePeriod(final long period);
  long getCacheUpdatePeriod();
  void setCacheUpdatePeriod(final long period);
}
