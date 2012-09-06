package ro.isdc.wro.cache;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;

/**
 * 
 * @author Alex Objelean
 */
public class TestDefaultSynchronizedCacheStrategyDecorator {
  private CacheStrategy<CacheEntry, ContentHashEntry> victim;
  
  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullObject() {
    DefaultSynchronizedCacheStrategyDecorator.decorate(null);
  }

  @Test
  public void shouldDecorateCacheStrategy() {
    CacheStrategy<CacheEntry, ContentHashEntry> original = new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>();
    victim = DefaultSynchronizedCacheStrategyDecorator.decorate(original);
    Assert.assertTrue(victim instanceof DefaultSynchronizedCacheStrategyDecorator);
    Assert.assertSame(original, ((CacheStrategyDecorator<?, ?>) victim).getDecoratedObject());
  }
  
  /**
   * Fix Issue 528:  Redundant CacheStrategy decoration (which has unclear cause, but it is safe to prevent redundant decoration anyway).
   */
  @Test
  public void shouldNotRedundantlyDecorateCacheStrategy() {
    final CacheStrategy<CacheEntry, ContentHashEntry> original = DefaultSynchronizedCacheStrategyDecorator.decorate(new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>());
    victim = DefaultSynchronizedCacheStrategyDecorator.decorate(original);
    Assert.assertTrue(victim instanceof DefaultSynchronizedCacheStrategyDecorator);
    Assert.assertSame(original, victim);
  }
}
