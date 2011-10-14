/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulates the logic which handles scheduler creation and destroy. This class is threadsafe.
 *
 * @author Alex Objelean
 * @created 14 Oct 2011
 * @since 1.4.2
 */
public class SchedulerHelper {
  private static final Logger LOG = LoggerFactory.getLogger(SchedulerHelper.class);
  private ScheduledExecutorService scheduler;
  /**
   * A factory providing the runnable to schedule.
   */
  private final ObjectFactory<Runnable> runnableFactory;
  /**
   * Period in seconds (how often a runnable should run).
   */
  private long period = 0;

  private SchedulerHelper(final ObjectFactory<Runnable> runnableFactory) {
    Validate.notNull(runnableFactory);
    this.runnableFactory = runnableFactory;
  }

  /**
   * Factory method. Creates a {@link SchedulerHelper} which consumes a factory providing a runnable. This approach
   * allows lazy runnable initialization.
   */
  public static SchedulerHelper create(final ObjectFactory<Runnable> runnableFactory) {
    return new SchedulerHelper(runnableFactory);
  }

  /**
   * Schedules with provided period using custom {@link TimeUnit}.
   *
   * @param period
   *          new period for scheduling.
   */
  public SchedulerHelper scheduleWithPeriod(final long period, final TimeUnit timeUnit) {
    Validate.notNull(timeUnit);
    LOG.debug("period: {}", period);
    LOG.debug("timeUnit: {}", timeUnit);
    if (this.period != period) {
      this.period = period;
      destroyScheduler();
      if (period > 0) {
        // Run a scheduled task which updates the model.
        // Here a scheduleWithFixedDelay is used instead of scheduleAtFixedRate because the later can cause a problem
        // (thread tries to make up for lost time in some situations)
        LOG.debug("Runing thread with period of {}", period);
        final Runnable runnable = runnableFactory.create();
        Validate.notNull(runnable);
        final ScheduledExecutorService scheduler = getScheduler();
        //avoid reject when this method is accessed concurrently.
        if (!scheduler.isShutdown()) {
          scheduler.scheduleWithFixedDelay(runnable, 0, period, timeUnit);
        }
      }
    }
    return this;
  }

  /**
   * Schedules with provided period using {@link TimeUnit#SECONDS} as a default time unit.
   *
   * @param period
   *          new period for scheduling.
   */
  public SchedulerHelper scheduleWithPeriod(final long period) {
    scheduleWithPeriod(period, TimeUnit.SECONDS);
    return this;
  }

  /**
   * Stops all jobs runned by the scheduler. It is important to call this method before application stops.
   */
  public void destroy() {
    destroyScheduler();
  }

  /**
   * Syncronous operation which ensure that all running jobs are stopped.
   */
  private void destroyScheduler() {
    LOG.debug("destroyScheduler");
    if (scheduler != null) {
      synchronized (this) {
        if (scheduler != null) {
          LOG.debug("Shutting down scheduler...");
          scheduler.shutdown();
          while (!scheduler.isTerminated()) {
            try {
              LOG.debug("\tawaiting scheduler termination...");
              scheduler.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
              LOG.debug("couldn't await scheduler termination", e);
            }
          }
          LOG.debug("[OK] Scheduler terminated successfully!");
          scheduler = null;
        }
      }
    }
  }

  /**
   * @return a not null scheduler.
   */
  private ScheduledExecutorService getScheduler() {
    if (scheduler == null) {
      synchronized(this) {
        if (scheduler == null) {
          scheduler = Executors.newSingleThreadScheduledExecutor(createDaemonThreadFactory());
        }
      }
    }
    return scheduler;
  }

  /**
   * @return {@link ThreadFactory} with daemon threads.
   */
  private static ThreadFactory createDaemonThreadFactory() {
    return new ThreadFactory() {
      public Thread newThread(final Runnable runnable) {
        final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName("wro4j-thread-daemon-thread");
        thread.setDaemon(true);
        return thread;
      }
    };
  }

  /**
   * @return the period
   */
  public long getPeriod() {
    return period;
  }
}
