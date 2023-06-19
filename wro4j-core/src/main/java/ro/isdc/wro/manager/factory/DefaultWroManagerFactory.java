package ro.isdc.wro.manager.factory;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ConfigConstants;
import ro.isdc.wro.config.support.PropertiesFactory;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Load the WroManagerFactory configured in {@link WroConfiguration} or loads a default one if none is configured.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public class DefaultWroManagerFactory
    implements WroManagerFactory {
  private final WroManagerFactory factory;

  /**
   * A factory method which uses {@link WroConfiguration} to get the configured wroManager className.
   *
   * @param configuration
   *          {@link WroConfiguration} to get the {@link ConfigConstants#managerFactoryClassName} from.
   */
  public static DefaultWroManagerFactory create(final WroConfiguration configuration) {
    return create(new ObjectFactory<WroConfiguration>() {
      @Override
      public WroConfiguration create() {
        return configuration;
      }
    });
  }

  public static DefaultWroManagerFactory create(final ObjectFactory<WroConfiguration> configurationFactory) {
    notNull(configurationFactory);
    final Properties properties = configurationFactory instanceof PropertiesFactory
        ? ((PropertiesFactory) configurationFactory).createProperties() : new Properties();
    final String wroManagerClassName = configurationFactory.create().getWroManagerClassName();
    if (wroManagerClassName != null) {
      properties.setProperty(ConfigConstants.managerFactoryClassName.getPropertyKey(), wroManagerClassName);
    }
    return new DefaultWroManagerFactory(properties);
  }

  /**
   * Responsible for creating an instance of {@link WroManagerFactory} whose className is configured in provided
   * {@link Properties}. If managerFactoryClassName is missing, a default one is created.
   *
   * @param properties
   *          {@link Properties} from where the managerFactoryClassName will be read in order to load an instance of
   *          {@link WroManagerFactory}.
   */
  public DefaultWroManagerFactory(final Properties properties) {
    notNull(properties);
    factory = initFactory(properties);
  }

  /**
   * Initialized inner factory based on provided configuration.
   */
  private WroManagerFactory initFactory(final Properties properties) {
    WroManagerFactory factory = null;
    final String wroManagerClassName = properties.getProperty(ConfigConstants.managerFactoryClassName.getPropertyKey());
    if (StringUtils.isEmpty(wroManagerClassName)) {
      // If no context param was specified we return the default factory
      factory = newManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass = null;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader().loadClass(wroManagerClassName);
        factory = (WroManagerFactory) factoryClass.getDeclaredConstructor().newInstance();
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception while loading WroManagerFactory class:" + wroManagerClassName, e);
      }
    }
    // add properties if required
    if (factory instanceof ConfigurableWroManagerFactory) {
      ((ConfigurableWroManagerFactory) factory).addConfigProperties(properties);
    }
    return factory;
  }

  /**
   * @VisibleForTesting
   * @return the default {@link WroManagerFactory} to be used when {@link WroConfiguration} doesn't specify any factory.
   *         Since 1.8.0, the default manager factory is {@link ConfigurableWroManagerFactory}.
   */
  WroManagerFactory newManagerFactory() {
    return new ConfigurableWroManagerFactory();
  }

  /**
   * {@inheritDoc}
   */
  public void onCachePeriodChanged(final long value) {
    factory.onCachePeriodChanged(value);
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged(final long value) {
    factory.onModelPeriodChanged(value);
  }

  /**
   * {@inheritDoc}
   */
  public WroManager create() {
    return factory.create();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    factory.destroy();
  }

  /**
   * @return the inner factory.
   */
  public final WroManagerFactory getFactory() {
    return factory;
  }

  @Override
  public String toString() {
    return factory.toString();
  }
}
