package ro.isdc.wro.manager.factory;


import static org.apache.commons.lang3.Validate.notNull;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManager;


/**
 * Load the WroManagerFactory configured in {@link WroConfiguration} or loads a default one if none is configured.
 *
 * @author Alex Objelean
 * @created 7 May 2012
 * @since 1.4.6
 */
public class DefaultWroManagerFactory
    implements WroManagerFactory {
  private final WroManagerFactory factory;

  /**
   *
   * @param configuration
   */
  public DefaultWroManagerFactory(final WroConfiguration configuration) {
    notNull(configuration);
    factory = initFactory(configuration.getWroManagerClassName());
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
    factory = initFactory(properties.getProperty(ConfigConstants.managerFactoryClassName.name()));
  }

  /**
   * Initialized inner factory based on provided configuration.
   */
  private WroManagerFactory initFactory(final String wroManagerClassName) {
    if (StringUtils.isEmpty(wroManagerClassName)) {
      // If no context param was specified we return the default factory
      return newManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass = null;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader().loadClass(wroManagerClassName);
        // Instantiate the factory
        return (WroManagerFactory)factoryClass.newInstance();
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception while loading WroManagerFactory class:" + wroManagerClassName, e);
      }
    }
  }

  /**
   * @return the default {@link WroManagerFactory} to be used when {@link WroConfiguration} doesn't specify any factory.
   */
  protected WroManagerFactory newManagerFactory() {
    return new BaseWroManagerFactory();
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
   * @VisibleForTesting
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
