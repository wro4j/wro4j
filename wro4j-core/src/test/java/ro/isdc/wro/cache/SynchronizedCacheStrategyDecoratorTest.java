package ro.isdc.wro.cache;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.cache.impl.MemoryCacheStrategy;

/**
 * @author Alex Objelean
 */
public class SynchronizedCacheStrategyDecoratorTest {
  private CacheStrategy<String, String> decorated;
  private SynchronizedCacheStrategyDecorator<String, String> victim;
  private Executor executor;
  
  @Before
  public void setUp() {
    decorated = new MemoryCacheStrategy<String, String>();
    executor = Executors.newCachedThreadPool();
  }
  
  
  @Test
  public void test() {
    victim = new SynchronizedCacheStrategyDecorator<String, String>(decorated) {
      @Override
      protected String loadValue(String key) {
        System.out.println("loadValue");
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return "v1";
      }
    };
    final String key1 = "key1";
    final Runnable getKeyRunnable = new Runnable() {
      public void run() {
        victim.get(key1);
      }
    }; 
    executor.execute(getKeyRunnable);
    executor.execute(getKeyRunnable);
    executor.execute(getKeyRunnable);
    victim.get(key1);
  }
  
}
