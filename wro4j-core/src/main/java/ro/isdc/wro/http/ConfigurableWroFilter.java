/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.http;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.factory.PropertyWroConfigurationFactory;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.ObjectFactory;

/**
 * An extension of {@link WroFilter} which allows configuration by injecting some of the properties. This class can be
 * very useful when using DelegatingFilterProxy (spring extension of Filter) and configuring the fields with values from
 * some properties file which may vary depending on environment.
 *
 * @author Alex Objelean
 */
public class ConfigurableWroFilter extends WroFilter {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableWroFilter.class);
  /**
   * Properties to be injected with default values set. These values are deprecated. Prefer setting the "properties"
   * field instead.
   */
  @Deprecated
  private boolean debug = true;
  @Deprecated
  private boolean gzipEnabled = true;
  @Deprecated
  private boolean jmxEnabled = true;
  @Deprecated
  private String mbeanName;
  @Deprecated
  private long cacheUpdatePeriod = 0;
  @Deprecated
  private long modelUpdatePeriod = 0;
  @Deprecated
  private boolean disableCache;
  @Deprecated
  private String encoding;

  /**
   * This {@link Properties} object will hold the configurations and it will replace all other fields.
   */
  private Properties properties;
  /**
   * {@inheritDoc}
   */
  @Override
  protected ObjectFactory<WroConfiguration> newWroConfigurationFactory() {
    if (properties == null) {
      //when no
      properties = new Properties();
      properties.setProperty(ConfigConstants.debug.name(), String.valueOf(debug));
      properties.setProperty(ConfigConstants.gzipResources.name(), String.valueOf(gzipEnabled));
      properties.setProperty(ConfigConstants.jmxEnabled.name(), String.valueOf(jmxEnabled));
      properties.setProperty(ConfigConstants.cacheUpdatePeriod.name(), String.valueOf(cacheUpdatePeriod));
      properties.setProperty(ConfigConstants.modelUpdatePeriod.name(), String.valueOf(modelUpdatePeriod));
      properties.setProperty(ConfigConstants.disableCache.name(), String.valueOf(disableCache));
      if (encoding != null) {
        properties.setProperty(ConfigConstants.encoding.name(), encoding);
      }
      if (mbeanName != null) {
        properties.setProperty(ConfigConstants.mbeanName.name(), mbeanName);  
      }
    }
    final PropertyWroConfigurationFactory factory = new PropertyWroConfigurationFactory(properties);
    return factory;
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
  protected String newMBeanName() {
    if (mbeanName != null) {
      return mbeanName;
    }
    return super.newMBeanName();
  }

  /**
   * The default implementation of ConfigurableWroFilter should allow setting of pre & post processors in configuration
   * properties. This will work only if no custom {@link WroManagerFactory} is configured.
   */
  @Override
  protected WroManagerFactory newWroManagerFactory() {
    return new BaseWroManagerFactory() {
      @Override
      protected ProcessorsFactory newProcessorsFactory() {
        final Map<String, ResourcePreProcessor> preProcessorsMap = ProcessorsUtils.createPreProcessorsMap();
        final Map<String, ResourcePostProcessor> postProcessorsMap = ProcessorsUtils.createPostProcessorsMap();
        LOG.debug("available preProcessors are: {}", preProcessorsMap.keySet());
        LOG.debug("available postProcessors are: {}", preProcessorsMap.keySet());
        //add processors from extensions module if one is available.
        LOG.debug("trying to import processors from extensions module...");
        try {
          final Class<?> clazz = Class.forName("ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory");
          final Method method = clazz.getMethod("populateMapWithExtensionsProcessors", Map.class);
          method.invoke(null, preProcessorsMap);
          method.invoke(null, postProcessorsMap);
          LOG.debug("[OK] Extensions processors imported successfully: {}", preProcessorsMap.keySet());
        } catch (final Exception e) {
          LOG.debug("[FAIL] Failed to import processors from extensions module", e);
        }

        return new ConfigurableProcessorsFactory().setPreProcessorsMap(preProcessorsMap).setPostProcessorsMap(
            postProcessorsMap).setProperties(properties);
      }
    };
  }

  /**
   * @param mbeanName the mbeanName to set
   */
  public void setMbeanName(final String mbeanName) {
    this.mbeanName = mbeanName;
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
   * @param properties the properties to set
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
   * @param encoding the encoding to set
   */
  public void setEncoding(final String encoding) {
    this.encoding = encoding;
  }
}
