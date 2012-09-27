package ro.isdc.wro.cache;

import java.util.Map;

import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.cache.spi.CacheStrategyProvider;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * Uses the {@link CacheStrategy} implementation associated with an alias read from properties file.
 * 
 * @author Alex Objelean
 * @created 22 Sep 2012
 * @since 1.5.0
 */
public class ConfigurableCacheStrategy
    extends AbstractConfigurableSingleStrategy<CacheStrategy, CacheStrategyProvider>
    implements CacheStrategy<CacheEntry, ContentHashEntry> {
  /**
   * Property name to specify alias.
   */
  public static final String KEY = "cacheStrategy";
  @Inject
  private Injector injector;
  private CacheStrategy<CacheEntry, ContentHashEntry> decorated;

  /**
   * {@inheritDoc}
   */
  public void clear() {
    getDecoratedStrategy().clear();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedStrategy().destroy();
  }

  /**
   * {@inheritDoc}
   */
  public ContentHashEntry get(final CacheEntry key) {
    return getDecoratedStrategy().get(key);
  }
  
  /**
   * {@inheritDoc}
   */
  public void put(final CacheEntry key, final ContentHashEntry value) {
    getDecoratedStrategy().put(key, value); 
  }
  
  private CacheStrategy<CacheEntry, ContentHashEntry> getDecoratedStrategy() {
    if (decorated == null) {
      decorated = getConfiguredStrategy();
      injector.inject(decorated);
    }
    return decorated;
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
  protected CacheStrategy<CacheEntry, ContentHashEntry> getDefaultStrategy() {
    return new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, CacheStrategy> getStrategies(final CacheStrategyProvider provider) {
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
