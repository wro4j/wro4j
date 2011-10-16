/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Objelean
 */
public class TestSchedulerHelper {
  private static final Logger LOG = LoggerFactory.getLogger(TestSchedulerHelper.class);
  private SchedulerHelper helper;

  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullArgument() {
    SchedulerHelper.create(null);
  }


  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullRunnable() {
    useNullRunnableWithPeriod(1);
  }

  @Test
  public void canAcceptNullRunnableWhenPeriodIsZero() {
    useNullRunnableWithPeriod(0);
  }

  @Test
  public void runnableNotStartedWhenPeriodIsZero() {
    createAndRunHelperForTest(createSleepingRunnable(1000), 0, TimeUnit.SECONDS);
  }

  @Test
  public void runLongRunningThread() throws Exception {
    createAndRunHelperForTest(createSleepingRunnable(100), 100, TimeUnit.MILLISECONDS);
    Thread.sleep(400);
  }

  @Test
  public void scheduleWithDifferentPeriods() throws Exception {
    helper = SchedulerHelper.create(new ObjectFactory<Runnable>() {
      public Runnable create() {
        return createSleepingRunnable(100);
      }
    });
    helper.scheduleWithPeriod(100);
    Assert.assertEquals(100, helper.getPeriod());
    Thread.sleep(200);

    helper.scheduleWithPeriod(200);
    Assert.assertEquals(200, helper.getPeriod());
    Thread.sleep(400);
  }

  @Test
  public void scheduleWithSamePeriods() throws Exception {
    helper = SchedulerHelper.create(new ObjectFactory<Runnable>() {
      public Runnable create() {
        return createSleepingRunnable(100);
      }
    });
    helper.scheduleWithPeriod(100);
    Assert.assertEquals(100, helper.getPeriod());
    Thread.sleep(200);

    helper.scheduleWithPeriod(100);
    Assert.assertEquals(100, helper.getPeriod());
    Thread.sleep(300);
  }

  @Test
  public void schedulerHelperIsSynchronized() throws Exception {
    helper = SchedulerHelper.create(new ObjectFactory<Runnable>() {
      public Runnable create() {
        return createSleepingRunnable(2000);
      }
    });
    final ThreadLocal<Long> period = new InheritableThreadLocal<Long>() {
      @Override
      protected Long initialValue() {
        return 0l;
      }
    };

    final ExecutorService service = Executors.newFixedThreadPool(5);
    for (int i = 0; i < 10; i++) {
      period.set(period.get() + 100);
      //Thread.sleep(300);
      service.execute(new Runnable() {
        public void run() {
          final long periodAsLong = period.get();
          helper.scheduleWithPeriod(periodAsLong, TimeUnit.MILLISECONDS);
        }
      });
    }
    Thread.sleep(5000);
    service.shutdown();
  }

  /**
   * creates a runnable which sleeps for a given period of time.
   *
   * @param period
   *          number of milliseconds to sleep.
   */
  private Runnable createSleepingRunnable(final long period) {
    return new Runnable() {
      public void run() {
        try {
          LOG.debug("\tTHREAD IS RUNNING.... ");
          Thread.sleep(period);
        } catch (final InterruptedException e) {
          LOG.debug("thread interrupted", e);
        }
      }
    };
  }

  private void useNullRunnableWithPeriod(final long period) {
    createAndRunHelperForTest(null, period, TimeUnit.SECONDS);
  }


  private void createAndRunHelperForTest(final Runnable runnable, final long period, final TimeUnit timeUnit) {
    helper = SchedulerHelper.create(new ObjectFactory<Runnable>() {
      public Runnable create() {
        return runnable;
      }
    });
    helper.scheduleWithPeriod(period, timeUnit);
  }

  @After
  public void tearDown() {
    if (helper != null) {
      helper.destroy();
    }
  }
}
