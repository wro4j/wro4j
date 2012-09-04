package ro.isdc.wro.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Objelean
 */
public class TestTaskExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(TestTaskExecutor.class);

  private TaskExecutor<Void> victim;
  
  @Before
  public void setUp() {
    victim = new TaskExecutor<Void>();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotSubmitNullCallables() throws Exception {
    victim.submit(null);
  }
  
  private Callable<Void> createSlowCallable(final long millis) {
    return new Callable<Void>() {
      public Void call()
          throws Exception {
        Thread.sleep(millis);
        return null;
      }
    };
  }
  
  @Test
  public void shouldHaveMinimalOverheadWhenRunningASingleTask() throws Exception {
    final Collection<Callable<Void>> callables = new ArrayList<Callable<Void>>();
    long delay = 100;
    callables.add(createSlowCallable(delay));
    
    long start = System.currentTimeMillis();
    victim.submit(callables);
    long end = System.currentTimeMillis();
    
    long delta = end - start;
    LOG.debug("Execution took: {}", delta);
    //number of milliseconds added by overhead 
    long overhead = 40;
    Assert.assertTrue(delta < delay + overhead);
  }
  
  @Test
  public void shouldBeFasterWhenRunningMultipleSlowTasks() throws Exception {
    final Collection<Callable<Void>> callables = new ArrayList<Callable<Void>>();
    long delay = 100;
    int times = 10;
    for (int i = 0; i < times; i++) {
      callables.add(createSlowCallable(delay));
    }
    
    long start = System.currentTimeMillis();
    victim.submit(callables);
    long end = System.currentTimeMillis();
    
    long delta = end - start;
    LOG.debug("Execution took: {}", delta);
    Assert.assertTrue(delta < delay * times);
  }
}
