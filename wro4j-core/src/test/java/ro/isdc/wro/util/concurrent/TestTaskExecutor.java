package ro.isdc.wro.util.concurrent;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.StopWatch;


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
  public void cannotSubmitNullCallables()
      throws Exception {
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
  public void shouldHaveMinimalOverheadWhenRunningASingleTask()
      throws Exception {
    final Collection<Callable<Void>> callables = new ArrayList<Callable<Void>>();
    final long delay = 100;
    callables.add(createSlowCallable(delay));

    final long start = System.currentTimeMillis();
    victim.submit(callables);
    final long end = System.currentTimeMillis();

    final long delta = end - start;
    LOG.debug("Execution took: {}", delta);
    //number of milliseconds added by overhead
    final long overhead = 40;
    assertTrue(delta < delay + overhead);
  }

  @Test
  public void shouldBeFasterWhenRunningMultipleSlowTasks()
      throws Exception {
    final Collection<Callable<Void>> callables = new ArrayList<Callable<Void>>();
    final long delay = 100;
    final int times = 10;
    for (int i = 0; i < times; i++) {
      callables.add(createSlowCallable(delay));
    }

    final long start = System.currentTimeMillis();
    victim.submit(callables);
    final long end = System.currentTimeMillis();

    final long delta = end - start;
    LOG.debug("Execution took: {}", delta);
    assertTrue(delta < delay * times);
  }

  private static void printDate() {
    LOG.debug(new SimpleDateFormat("HH:ss:S").format(new Date()));
  }

  public static void main(final String[] args)
      throws Exception {
    final TaskExecutor<String> executor = new TaskExecutor<String>() {
      @Override
      protected void onResultAvailable(final String result)
          throws Exception {
        LOG.debug("<<< [OK] result available: " + result);
        LOG.debug("\n===============\n");
        printDate();
      }
    };
    final List<Callable<String>> tasks = new ArrayList<Callable<String>>();
    tasks.add(new Callable<String>() {
      public String call()
          throws Exception {
        printDate();
        final String result = Thread.currentThread().getName();
        LOG.debug("\n>>> [START]===============\nThread " + result + " started...");
        final double sleep = 1000;
        LOG.debug("Sleeping for: " + sleep);
        Thread.sleep((long) sleep);
        return result;
      }
    });
    final int index = 100;
    for (int i = 0; i < index; i++) {
      tasks.add(new Callable<String>() {
        public String call()
            throws Exception {
          printDate();
          final String result = Thread.currentThread().getName() + ":" + UUID.randomUUID().toString();
          LOG.debug("\n[START]===============\nThread " + result + " started...");
          final double sleep = Math.random() * 300;
          LOG.debug("Sleeping for: " + sleep);
          Thread.sleep((long) sleep);
          return result;
        }
      });
    }
    final StopWatch watch = new StopWatch();
    watch.start("submit tasks");
    executor.submit(tasks);
    watch.stop();
    LOG.debug(watch.prettyPrint());
  }
}
