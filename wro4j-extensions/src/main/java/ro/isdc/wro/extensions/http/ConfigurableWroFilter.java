/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.http;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.WroFilter;


/**
 * An extension of {@link WroFilter} which allows configuration by injecting some of the properties.
 *
 * @author Alex Objelean
 */
public class ConfigurableWroFilter extends WroFilter {
  private boolean debug = true;
  private boolean gzipEnabled = true;
  private long cacheUpdatePeriod = 0;
  private long modelUpdatePeriod = 0;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initConfiguration(final WroConfiguration configuration) {
    //use injected properties for configuration
    configuration.setCacheUpdatePeriod(cacheUpdatePeriod);
    configuration.setModelUpdatePeriod(modelUpdatePeriod);
    configuration.setDebug(debug);
    configuration.setGzipEnabled(gzipEnabled);
  }

  /**
   * @param debug the debug to set
   */
  public final void setDebug(final boolean debug) {
    this.debug = debug;
  }

  /**
   * @param gzipEnabled the gzipEnabled to set
   */
  public final void setGzipEnabled(final boolean gzipEnabled) {
    this.gzipEnabled = gzipEnabled;
  }

  /**
   * @param cacheUpdatePeriod the cacheUpdatePeriod to set
   */
  public final void setCacheUpdatePeriod(final long cacheUpdatePeriod) {
    this.cacheUpdatePeriod = cacheUpdatePeriod;
  }

  /**
   * @param modelUpdatePeriod the modelUpdatePeriod to set
   */
  public final void setModelUpdatePeriod(final long modelUpdatePeriod) {
    this.modelUpdatePeriod = modelUpdatePeriod;
  }
}
