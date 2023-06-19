package ro.isdc.wro.cache.support;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.util.AbstractDecorator;

/**
 * A decorator of {@link CacheStrategy}.
 * 
 * @author Alex Objelean
 * @since 1.4.6
 */
public class CacheStrategyDecorator<K, V> extends AbstractDecorator<CacheStrategy<K, V>>
    implements CacheStrategy<K, V>  {

  public CacheStrategyDecorator(final CacheStrategy<K,V> decorated) {
    super(decorated);
  }
  
  /**
   * {@inheritDoc}
   */
  public void put(K key, V value) {
    getDecoratedObject().put(key, value);
  }

  /**
   * {@inheritDoc}
   */
  public V get(K key) {
    return getDecoratedObject().get(key);
  }

  /**
   * {@inheritDoc}
   */
  public void clear() {
    getDecoratedObject().clear();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedObject().destroy();
  }
}
