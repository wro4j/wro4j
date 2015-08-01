package ro.isdc.wro.cache.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
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
import ro.isdc.wro.model.resource.support.change.ResourceWatcher;
import ro.isdc.wro.util.ObjectDecorator;
import ro.isdc.wro.util.SchedulerHelper;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestDefaultSynchronizedCacheStrategyDecorator {
  private static final String GROUP_NAME = "g1";
  private static final String RESOURCE_URI = "/test.js";

  private DefaultSynchronizedCacheStrategyDecorator victim;
  @Mock
  private ResourceWatcher mockResourceWatcher;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new DefaultSynchronizedCacheStrategyDecorator(new MemoryCacheStrategy<CacheKey, CacheValue>()) {
      @Override
      TimeUnit getTimeUnitForResourceWatcher() {
        // use milliseconds to make test faster
        return TimeUnit.MILLISECONDS;
      }
    };
    createInjector().inject(victim);
  }

  @After
  public void tearDown() {
    victim.destroy();
    Context.unset();
    // have to reset it, otherwise a test fails when testing entire project.
    Mockito.reset(mockResourceWatcher);
  }

  public Injector createInjector() {
    final WroModel model = new WroModel().addGroup(new Group(GROUP_NAME).addResource(Resource.create(RESOURCE_URI)));
    final WroModelFactory modelFactory = WroTestUtils.simpleModelFactory(model);
    final UriLocatorFactory locatorFactory = WroTestUtils.createResourceMockingLocatorFactory();
    final BaseWroManagerFactory factory = new BaseWroManagerFactory().setModelFactory(modelFactory).setUriLocatorFactory(
        locatorFactory);
    factory.setProcessorsFactory(new SimpleProcessorsFactory());
    return InjectorBuilder.create(factory).setResourceWatcher(mockResourceWatcher).build();
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullKey() {
    victim.get(null);
  }

  @Test
  public void shouldNotCheckForChangesWhenResourceWatcherPeriodIsNotSet()
      throws Exception {
    final CacheKey key = new CacheKey("g1", ResourceType.JS, true);
    victim.get(key);
    victim.get(key);
    verify(mockResourceWatcher, never()).check(key);
  }

  /**
   * Proves that even if the get() is invoked more times, the check is performed only after a certain period of time.
   */
  @Test
  public void shouldCheckOnlyAfterTimeout()
      throws Exception {
    final long updatePeriod = 10;
    final long delta = 5;
    Context.get().getConfig().setResourceWatcherUpdatePeriod(updatePeriod);
    final CacheKey key = new CacheKey("g1", ResourceType.JS, true);
    when(mockResourceWatcher.tryAsyncCheck(Mockito.eq(key))).thenReturn(true);
    final long start = System.currentTimeMillis();
    do {
      victim.get(key);
    } while (System.currentTimeMillis() - start < updatePeriod - delta);
    verify(mockResourceWatcher, times(1)).tryAsyncCheck(key);
  }

  /**
   * This test does not pass consistently. TODO: rewrite it in order to make it always pass.
   */
  @Ignore
  @Test
  public void shouldCheckDifferentGroups()
      throws Exception {
    final long updatePeriod = 10;
    final long delta = 4;
    Context.get().getConfig().setResourceWatcherUpdatePeriod(updatePeriod);
    final CacheKey key1 = new CacheKey(GROUP_NAME, ResourceType.JS, true);
    final CacheKey key2 = new CacheKey(GROUP_NAME, ResourceType.CSS, true);
    final long start = System.currentTimeMillis();
    victim.get(key1);
    Thread.sleep(updatePeriod);
    do {
      victim.get(key1);
    } while (System.currentTimeMillis() - start < updatePeriod - delta);
    victim.get(key2);
    verify(mockResourceWatcher, times(2)).check(key1);
    verify(mockResourceWatcher, times(1)).check(key2);
  }

  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullObject() {
    DefaultSynchronizedCacheStrategyDecorator.decorate(null);
  }

  @Test
  public void shouldDecorateCacheStrategy() {
    final CacheStrategy<CacheKey, CacheValue> original = new LruMemoryCacheStrategy<CacheKey, CacheValue>();
    victim = (DefaultSynchronizedCacheStrategyDecorator) DefaultSynchronizedCacheStrategyDecorator.decorate(original);
    assertTrue(victim instanceof DefaultSynchronizedCacheStrategyDecorator);
    assertSame(original, ((ObjectDecorator<?>) victim).getDecoratedObject());
  }

  /**
   * Fix Issue 528: Redundant CacheStrategy decoration (which has unclear cause, but it is safe to prevent redundant
   * decoration anyway).
   */
  @Test
  public void shouldNotRedundantlyDecorateCacheStrategy() {
    final CacheStrategy<CacheKey, CacheValue> original = DefaultSynchronizedCacheStrategyDecorator.decorate(new LruMemoryCacheStrategy<CacheKey, CacheValue>());
    victim = (DefaultSynchronizedCacheStrategyDecorator) DefaultSynchronizedCacheStrategyDecorator.decorate(original);
    assertTrue(victim instanceof DefaultSynchronizedCacheStrategyDecorator);
    assertSame(original, victim);
  }

  @Test
  public void shouldDestroySchedulerWhenStrategyIsDestroyed() {
    final SchedulerHelper scheduler = Mockito.mock(SchedulerHelper.class);
    victim = new DefaultSynchronizedCacheStrategyDecorator(new MemoryCacheStrategy<CacheKey, CacheValue>()) {
      @Override
      SchedulerHelper newResourceWatcherScheduler() {
        return scheduler;
      };
    };
    victim.destroy();
    verify(scheduler).destroy();
  }

  @Test
  public void shouldNotWatchForChangeUnlessCheckCompleted() {
    Context.get().getConfig().setResourceWatcherUpdatePeriod(100);
    final CacheKey key = new CacheKey(GROUP_NAME, ResourceType.JS, true);

    when(mockResourceWatcher.tryAsyncCheck(Mockito.eq(key))).thenReturn(false);
    victim.get(key);
    assertFalse(victim.wasCheckedForChange(key));

    when(mockResourceWatcher.tryAsyncCheck(Mockito.eq(key))).thenReturn(true);
    victim.get(key);
    assertTrue(victim.wasCheckedForChange(key));
  }
}
