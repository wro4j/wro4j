package ro.isdc.wro.cache;

import java.util.Map;

import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.cache.spi.CacheStrategyProvider;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * Uses the {@link CacheStrategy} implementation associated with an alias read from properties file.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class ConfigurableCacheStrategy
    extends AbstractConfigurableSingleStrategy<CacheStrategy<CacheKey, CacheValue>, CacheStrategyProvider>
    implements CacheStrategy<CacheKey, CacheValue> {
  /**
   * Property name to specify alias.
   */
  public static final String KEY = "cacheStrategy";

  /**
   * {@inheritDoc}
   */
  public void clear() {
    getConfiguredStrategy().clear();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getConfiguredStrategy().destroy();
  }

  /**
   * {@inheritDoc}
   */
  public CacheValue get(final CacheKey key) {
    return getConfiguredStrategy().get(key);
  }

  /**
   * {@inheritDoc}
   */
  public void put(final CacheKey key, final CacheValue value) {
    getConfiguredStrategy().put(key, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getStrategyKey() {
    return KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CacheStrategy<CacheKey, CacheValue> getDefaultStrategy() {
    return new LruMemoryCacheStrategy<CacheKey, CacheValue>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, CacheStrategy<CacheKey, CacheValue>> getStrategies(final CacheStrategyProvider provider) {
    return provider.provideCacheStrategies();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<CacheStrategyProvider> getProviderClass() {
    return CacheStrategyProvider.class;
  }
}
