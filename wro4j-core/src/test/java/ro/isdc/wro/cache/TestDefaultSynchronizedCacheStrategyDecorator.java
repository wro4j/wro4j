package ro.isdc.wro.cache;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.support.ResourceWatcher;
import ro.isdc.wro.util.ObjectDecorator;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestDefaultSynchronizedCacheStrategyDecorator {
  private static final String GROUP_NAME = "g1";
  private static final String RESOURCE_URI = "/test.js";

  private CacheStrategy<CacheEntry, ContentHashEntry> victim;
  private ResourceWatcher mockResourceWatcher; 
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new DefaultSynchronizedCacheStrategyDecorator(new MemoryCacheStrategy<CacheEntry, ContentHashEntry>()) {
      @Override
      TimeUnit getTimeUnitForResourceWatcher() {
        //use milliseconds to make test faster
        return TimeUnit.MILLISECONDS;
      }
      
      @Override
      ResourceWatcher getResourceWatcher() {
        if (mockResourceWatcher == null) {
          mockResourceWatcher = Mockito.spy(super.getResourceWatcher());
        }
        return mockResourceWatcher;
      }
    };
    createInjector().inject(victim);
    //invoke getter explicitly to be sure we have a not null reference
    ((DefaultSynchronizedCacheStrategyDecorator) victim).getResourceWatcher();
  }
  
  public Injector createInjector() {
    final WroModel model = new WroModel().addGroup(new Group(GROUP_NAME).addResource(Resource.create(RESOURCE_URI)));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final UriLocatorFactory locatorFactory = WroTestUtils.createResourceMockingLocatorFactory();
    final BaseWroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory);
    factory.setProcessorsFactory(new SimpleProcessorsFactory());
    final Injector injector = InjectorBuilder.create(factory).build();
    return injector;
  }

  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullKey() {
    victim.get(null);
  }
  
  @Test
  public void shouldNotCheckForChangesWhenResourceWatcherPeriodIsNotSet() throws Exception {
    final CacheEntry key = new CacheEntry("g1", ResourceType.JS, true);
    victim.get(key);
    victim.get(key);
    Mockito.verify(mockResourceWatcher, never()).check(key);
  }
  
  /**
   * Proves that even if the get() is invoked more times, the check is performed only after a certain period of time.
   */
  @Test
  public void shouldCheckOnlyAfterTimeout() throws Exception {
    final long updatePeriod = 100;
    Context.get().getConfig().setResourceWatcherUpdatePeriod(updatePeriod);
    final CacheEntry key = new CacheEntry("g1", ResourceType.JS, true);
    long start = System.currentTimeMillis();
    while(System.currentTimeMillis() - start < updatePeriod) {
      victim.get(key);
    }
    victim.get(key);
    Mockito.verify(mockResourceWatcher, times(2)).check(key);
  }
  
  @Test
  public void shouldCheckDifferentGroups() throws Exception {
    final long updatePeriod = 100;
    Context.get().getConfig().setResourceWatcherUpdatePeriod(updatePeriod);
    final CacheEntry key1 = new CacheEntry(GROUP_NAME, ResourceType.JS, true);
    final CacheEntry key2 = new CacheEntry(GROUP_NAME, ResourceType.CSS, true);
    long start = System.currentTimeMillis();
    while(System.currentTimeMillis() - start < updatePeriod) {
      victim.get(key1);
    }
    victim.get(key2);
    Mockito.verify(mockResourceWatcher, times(2)).check(key1);
    Mockito.verify(mockResourceWatcher, times(1)).check(key2);
  }

  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullObject() {
    DefaultSynchronizedCacheStrategyDecorator.decorate(null);
  }

  @Test
  public void shouldDecorateCacheStrategy() {
    CacheStrategy<CacheEntry, ContentHashEntry> original = new LruMemoryCacheStrategy<CacheEntry, ContentHashEntry>();
    victim = DefaultSynchronizedCacheStrategyDecorator.decorate(original);
    Assert.assertTrue(victim instanceof DefaultSynchronizedCacheStrategyDecorator);
    Assert.assertSame(original, ((ObjectDecorator<?>) victim).getDecoratedObject());
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
