/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.http;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;

import jakarta.servlet.FilterConfig;
import ro.isdc.wro.config.factory.PropertyWroConfigurationFactory;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.manager.factory.DefaultWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.util.ObjectFactory;


/**
 * An extension of {@link WroFilter} which allows configuration by injecting some of the properties. This class can be
 * very useful when using DelegatingFilterProxy (spring extension of Filter) and configuring the fields with values from
 * some properties file which may vary depending on environment.
 *
 * @author Alex Objelean
 */
public class ConfigurableWroFilter
    extends WroFilter {
  /**
   * Properties to be injected with default values set. These values are deprecated. Prefer setting the "properties"
   * field instead.
   */
  @Deprecated
  private boolean debug = BooleanUtils.isTrue((Boolean) ConfigConstants.debug.getDefaultPropertyValue());
  @Deprecated
  private boolean gzipEnabled = BooleanUtils.isTrue((Boolean) ConfigConstants.gzipResources.getDefaultPropertyValue());
  @Deprecated
  private boolean jmxEnabled = BooleanUtils.isTrue((Boolean) ConfigConstants.jmxEnabled.getDefaultPropertyValue());
  @Deprecated
  private String mbeanName = (String) ConfigConstants.mbeanName.getDefaultPropertyValue();
  @Deprecated
  private long cacheUpdatePeriod  = (Long) ConfigConstants.cacheUpdatePeriod.getDefaultPropertyValue();
  @Deprecated
  private long modelUpdatePeriod  = (Long) ConfigConstants.modelUpdatePeriod.getDefaultPropertyValue();
  @Deprecated
  private boolean disableCache = BooleanUtils.isTrue((Boolean) ConfigConstants.disableCache.getDefaultPropertyValue());
  @Deprecated
  private String encoding = (String) ConfigConstants.encoding.getDefaultPropertyValue();

  /**
   * This {@link Properties} object will hold the configurations and it will replace all other fields.
   */
  private Properties properties;

  /**
   * {@inheritDoc}
   */
  @Override
  protected ObjectFactory<WroConfiguration> newWroConfigurationFactory(final FilterConfig filterConfig) {
    if (properties == null) {
      // when no
      properties = new Properties();
      properties.setProperty(ConfigConstants.debug.getPropertyKey(), String.valueOf(debug));
      properties.setProperty(ConfigConstants.gzipResources.getPropertyKey(), String.valueOf(gzipEnabled));
      properties.setProperty(ConfigConstants.jmxEnabled.getPropertyKey(), String.valueOf(jmxEnabled));
      properties.setProperty(ConfigConstants.cacheUpdatePeriod.getPropertyKey(), String.valueOf(cacheUpdatePeriod));
      properties.setProperty(ConfigConstants.modelUpdatePeriod.getPropertyKey(), String.valueOf(modelUpdatePeriod));
      properties.setProperty(ConfigConstants.disableCache.getPropertyKey(), String.valueOf(disableCache));
      if (encoding != null) {
        properties.setProperty(ConfigConstants.encoding.getPropertyKey(), encoding);
      }
      if (mbeanName != null) {
        properties.setProperty(ConfigConstants.mbeanName.getPropertyKey(), mbeanName);
      }
    }
    return new PropertyWroConfigurationFactory(properties);
  }

  /**
   * @param disableCache
   *          the disableCache to set
   */
  public void setDisableCache(final boolean disableCache) {
    this.disableCache = disableCache;
  }

  @Override
  protected String newMBeanName() {
    if (mbeanName != null) {
      return mbeanName;
    }
    return super.newMBeanName();
  }

  /**
   * The default implementation of ConfigurableWroFilter should allow setting of pre and post processors in configuration
   * properties. This will work only if no custom {@link WroManagerFactory} is configured.
   */
  @Override
  protected WroManagerFactory newWroManagerFactory() {
    return new DefaultWroManagerFactory(properties);
  }

  /**
   * @param mbeanName
   *          the mbeanName to set
   */
  public void setMbeanName(final String mbeanName) {
    this.mbeanName = mbeanName;
  }

  /**
   * @param jmxEnabled
   *          the jmxEnabled to set
   */
  public void setJmxEnabled(final boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
  }

  /**
   * @param debug
   *          the debug to set
   */
  public final void setDebug(final boolean debug) {
    this.debug = debug;
  }

  /**
   * @param gzipEnabled
   *          the gzipEnabled to set
   */
  public final void setGzipEnabled(final boolean gzipEnabled) {
    this.gzipEnabled = gzipEnabled;
  }

  /**
   * @param cacheUpdatePeriod
   *          the cacheUpdatePeriod to set
   */
  public final void setCacheUpdatePeriod(final long cacheUpdatePeriod) {
    this.cacheUpdatePeriod = cacheUpdatePeriod;
  }

  /**
   * @param modelUpdatePeriod
   *          the modelUpdatePeriod to set
   */
  public final void setModelUpdatePeriod(final long modelUpdatePeriod) {
    this.modelUpdatePeriod = modelUpdatePeriod;
  }

  /**
   * @param properties
   *          the properties to set
   */
  public void setProperties(final Properties properties) {
    this.properties = properties;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return this.encoding;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding(final String encoding) {
    this.encoding = encoding;
  }
}
