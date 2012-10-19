package ro.isdc.wro.manager.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.WroRuntimeException;
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

  public DefaultWroManagerFactory(final WroConfiguration configuration) {
    factory = initFactory(configuration);
  }

  /**
   * Initialized inner factory based on provided configuration.
   */
  private WroManagerFactory initFactory(final WroConfiguration configuration) {
    Validate.notNull(configuration);
    if (StringUtils.isEmpty(configuration.getWroManagerClassName())) {
      // If no context param was specified we return the default factory
      return newManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass = null;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader().loadClass(
          configuration.getWroManagerClassName());
        // Instantiate the factory
        return (WroManagerFactory)factoryClass.newInstance();
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception while loading WroManagerFactory class", e);
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
