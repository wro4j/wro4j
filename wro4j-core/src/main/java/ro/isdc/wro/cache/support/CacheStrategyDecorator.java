package ro.isdc.wro.cache.support;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.util.AbstractDecorator;

/**
 * A decorator of {@link CacheStrategy}.
 * 
 * @author Alex Objelean
 * @created 30 Apr 2012
 * @since 1.4.6
 */
public class CacheStrategyDecorator<K, V> extends AbstractDecorator<CacheStrategy<K, V>>
    implements CacheStrategy<K, V>, StaleCacheKeyAware<K>  {

  public CacheStrategyDecorator(final CacheStrategy<K,V> decorated) {
    super(decorated);
  }
  
  public void put(K key, V value) {
    getDecoratedObject().put(key, value);
  }

  public V get(K key) {
    return getDecoratedObject().get(key);
  }

  public void clear() {
    getDecoratedObject().clear();
  }

  public void destroy() {
    getDecoratedObject().destroy();
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  public boolean isStale(K key) {
    return isStaleCacheKeyAware() && ((StaleCacheKeyAware) getDecoratedObject()).isStale(key);
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void markAsStale(K key) {
    if (isStaleCacheKeyAware()) {
      ((StaleCacheKeyAware) getDecoratedObject()).markAsStale(key);
    } 
  }

  private boolean isStaleCacheKeyAware() {
    return getDecoratedObject() instanceof StaleCacheKeyAware;
  }
}
