/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestSchedulerHelper {
  private static final Logger LOG = LoggerFactory.getLogger(TestSchedulerHelper.class);
  @Mock
  private Runnable mockRunnable;
  private SchedulerHelper helper;
  
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
    initMocks(this);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArgument() {
    SchedulerHelper.create(null);
  }
  
  @Test(expected = NullPointerException.class)
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
  public void runLongRunningThread()
      throws Exception {
    createAndRunHelperForTest(createSleepingRunnable(100), 100, TimeUnit.MILLISECONDS);
    Thread.sleep(400);
  }
  
  @Test
  public void scheduleWithDifferentPeriods()
      throws Exception {
    helper = SchedulerHelper.create(new DestroyableLazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return createSleepingRunnable(10);
      }
    });
    helper.scheduleWithPeriod(10);
    Assert.assertEquals(10, helper.getPeriod());
    Thread.sleep(20);
    
    helper.scheduleWithPeriod(20);
    Assert.assertEquals(20, helper.getPeriod());
    Thread.sleep(40);
  }
  
  @Test
  public void scheduleWithSamePeriods()
      throws Exception {
    helper = SchedulerHelper.create(new DestroyableLazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return createSleepingRunnable(10);
      }
    });
    helper.scheduleWithPeriod(10);
    Assert.assertEquals(10, helper.getPeriod());
    Thread.sleep(20);
    
    helper.scheduleWithPeriod(10);
    Assert.assertEquals(10, helper.getPeriod());
    Thread.sleep(30);
  }
  
  @Test
  public void schedulerHelperIsSynchronized()
      throws Exception {
    helper = SchedulerHelper.create(new DestroyableLazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new Runnable() {
          public void run() {
            try {
              LOG.debug("\tRunning thread ...");
              Thread.sleep(40);
            } catch (final Exception e) {
              LOG.error("runnable interrupted", e);
            }
          }
        };
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
      period.set(period.get() + 30);
      // Thread.sleep(300);
      service.execute(new Runnable() {
        public void run() {
          helper.scheduleWithPeriod(period.get(), TimeUnit.MILLISECONDS);
        }
      });
    }
    Thread.sleep(400);
    helper.destroy();
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
          Thread.sleep(period);
        } catch (final Exception e) {
          LOG.error("thread interrupted", e);
        }
      }
    };
  }
  
  private void useNullRunnableWithPeriod(final long period) {
    createAndRunHelperForTest(null, period, TimeUnit.SECONDS);
  }
  
  private void createAndRunHelperForTest(final Runnable runnable, final long period, final TimeUnit timeUnit) {
    helper = SchedulerHelper.create(new DestroyableLazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return runnable;
      }
    });
    helper.scheduleWithPeriod(period, timeUnit);
  }
  
  @Test
  public void shouldNotInvokeRunnableImmediatelyAfterScheduleIsInvoked()
      throws Exception {
    helper = SchedulerHelper.create(new DestroyableLazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return mockRunnable;
      }
    });
    helper.scheduleWithPeriod(7200);
    Thread.sleep(10);
    verify(mockRunnable, Mockito.never()).run();
  }
  
  @After
  public void tearDown() {
    Context.unset();
    if (helper != null) {
      helper.destroy();
    }
  }
}
