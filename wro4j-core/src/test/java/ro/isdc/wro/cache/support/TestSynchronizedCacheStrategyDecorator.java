package ro.isdc.wro.cache.support;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.impl.MemoryCacheStrategy;
import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestSynchronizedCacheStrategyDecorator {
  private CacheStrategy<String, String> decorated;
  private AbstractSynchronizedCacheStrategyDecorator<String, String> victim;
  private ExecutorService executor;
  
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
    decorated = new MemoryCacheStrategy<String, String>();
    executor = Executors.newCachedThreadPool();
  }
  
  @After
  public void tearDown()
      throws Exception {
    awaitTermination();
    executor.shutdown();
  }
  
  @Test
  public void shouldInvokeLoadOnlyOnceForTheSameKey()
      throws Exception {
    final AtomicInteger count = createSlowCountingDecorator();
    final String key = "key";
    executor.execute(getRunnableForKey(key));
    executor.execute(getRunnableForKey(key));
    executor.execute(getRunnableForKey(key));
    executor.execute(getRunnableForKey(key));
    executor.execute(getRunnableForKey(key));
    awaitTermination();
    Assert.assertEquals(1, count.get());
  }
  
  @Test
  public void shouldInvokeLoadTwoTimesForDifferentKeys()
      throws Exception {
    final AtomicInteger count = createSlowCountingDecorator();
    final String key1 = "key1";
    final String key2 = "key2";
    executor.execute(getRunnableForKey(key1));
    executor.execute(getRunnableForKey(key1));
    executor.execute(getRunnableForKey(key2));
    executor.execute(getRunnableForKey(key1));
    executor.execute(getRunnableForKey(key2));
    executor.execute(getRunnableForKey(key1));
    executor.execute(getRunnableForKey(key2));
    executor.execute(getRunnableForKey(key1));
    awaitTermination();
    Assert.assertEquals(2, count.get());
  }
  
  protected AtomicInteger createSlowCountingDecorator() {
    final AtomicInteger count = new AtomicInteger();
    victim = new AbstractSynchronizedCacheStrategyDecorator<String, String>(decorated) {
      @Override
      protected String loadValue(final String key) {
        try {
          Thread.sleep(300);
          count.incrementAndGet();
        } catch (final InterruptedException e) {
          throw new RuntimeException(e);
        }
        return "value-" + key;
      }
    };
    return count;
  }
  
  /**
   * Await for executor termination
   */
  private void awaitTermination() {
    try {
      executor.awaitTermination(400, TimeUnit.MILLISECONDS);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  protected Runnable getRunnableForKey(final String key) {
    return new Runnable() {
      public void run() {
        victim.get(key);
      }
    };
  }
}
