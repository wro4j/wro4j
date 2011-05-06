/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.http;

/**
 * An extension of {@link WroFilter} which allows configuration by injecting some of the properties. This class can be
 * very useful when using DelegatingFilterProxy (spring extension of Filter) and configuring the fields with values from
 * some properties file which may vary depending on environment.
 *
 * @author Alex Objelean
 */
public class ConfigurableWroFilter extends WroFilter {
  /**
   * Properties to be injected with default values set.
   */
  private boolean debug = true;
  private boolean gzipEnabled = true;
  private boolean jmxEnabled = true;
  private String mbeanName;
  private long cacheUpdatePeriod = 0;
  private long modelUpdatePeriod = 0;
  private boolean disableCache;
  private String encoding;

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isDebug() {
    return debug;
  }

  /**
   * @return the disableCache
   */
  @Override
  public boolean isDisableCache() {
    return this.disableCache;
  }

  /**
   * @param disableCache the disableCache to set
   */
  public void setDisableCache(final boolean disableCache) {
    this.disableCache = disableCache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isGzipResources() {
    return gzipEnabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String newMBeanName() {
    if (mbeanName != null) {
      return mbeanName;
    }
    return super.newMBeanName();
  }

  /**
   * @param mbeanName the mbeanName to set
   */
  public void setMbeanName(final String mbeanName) {
    this.mbeanName = mbeanName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected long getCacheUpdatePeriod() {
    return cacheUpdatePeriod;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected long getModelUpdatePeriod() {
    return modelUpdatePeriod;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean getJmxEnabled() {
    return jmxEnabled;
  }

  /**
   * @param jmxEnabled the jmxEnabled to set
   */
  public void setJmxEnabled(final boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
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

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return this.encoding;
  }

  /**
   * @param encoding the encoding to set
   */
  public void setEncoding(final String encoding) {
    this.encoding = encoding;
  }
}
