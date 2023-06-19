/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.util;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulates the logic which handles scheduler creation and destroy. This class is thread-safe.
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
public class SchedulerHelper {
  private static final Logger LOG = LoggerFactory.getLogger(SchedulerHelper.class);


  private final LazyInitializer<ScheduledThreadPoolExecutor> poolInitializer = new LazyInitializer<ScheduledThreadPoolExecutor>() {
    @Override
    protected ScheduledThreadPoolExecutor initialize() {
      return new ScheduledThreadPoolExecutor(1, WroUtil.createDaemonThreadFactory(name)) {
        @Override
        public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
          return false;
        };
      };
    }
  };

  /**
   * An initializer providing the runnable to schedule.
   */
  private final LazyInitializer<Runnable> lazyRunnable;
  /**
   * Period in seconds (how often a runnable should run).
   */
  private volatile long period = 0;
  /**
   * The name of this scheduler (used for logging).
   */
  private final String name;
  /**
   * The future of the currently running task. Allows reschedule operation by canceling execution of the running
   * thread.
   */
  private ScheduledFuture<?> future;

  private SchedulerHelper(final LazyInitializer<Runnable> lazyRunnable) {
    this(lazyRunnable, null);
  }

  private SchedulerHelper(final LazyInitializer<Runnable> lazyRunnable, final String name) {
    notNull(lazyRunnable);
    this.name = name;
    this.lazyRunnable = lazyRunnable;
  }

  /**
   * Factory method. Creates a {@link SchedulerHelper} which consumes a factory providing a runnable. This approach
   * allows lazy runnable initialization.
   *
   * @param runnableFactory
   *          a factory creating the runnable to schedule.
   * @param name
   *          the name associated with this {@link SchedulerHelper} (useful to detect if this class is causing a memory
   *          leak.
   */
  public static SchedulerHelper create(final LazyInitializer<Runnable> runnableFactory, final String name) {
    return new SchedulerHelper(runnableFactory, name);
  }

  /**
   * Factory method. Creates a {@link SchedulerHelper} which consumes a factory providing a runnable. This approach
   * allows lazy runnable initialization.
   *
   * @param runnableFactory
   *          a factory creating the runnable to schedule.
   */
  public static SchedulerHelper create(final LazyInitializer<Runnable> runnableFactory) {
    return new SchedulerHelper(runnableFactory);
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
   * Run the scheduler with the provided period of time. If the scheduler is already started, it will be stopped (not
   * before the running job is complete).
   *
   * @param period
   *          new period for scheduling.
   * @param timeUnit
   *          what kind of time unit is associated with the period.
   */
  public SchedulerHelper scheduleWithPeriod(final long period, final TimeUnit timeUnit) {
    notNull(timeUnit);
    LOG.debug("period: {} [{}]", period, timeUnit);
    if (this.period != period) {
      this.period = period;
      if (!poolInitializer.get().isShutdown()) {
        startScheduler(period, timeUnit);
      } else {
        LOG.warn("Cannot schedule because destroy was already called!");
      }
    }
    return this;
  }

  private synchronized void startScheduler(final long period, final TimeUnit timeUnit) {
    // let the running task to finish its job.
    cancelRunningTask();
    if (period > 0) {
      // scheduleWithFixedDelay is used instead of scheduleAtFixedRate because the later can cause a problem
      // (thread tries to make up for lost time in some situations)
      final Runnable runnable = lazyRunnable.get();
      notNull(runnable);
      // avoid reject when this method is accessed concurrently.
      if (!poolInitializer.get().isShutdown()) {
        LOG.debug("[START] Scheduling thread with period of {} {}. ThreadId:  {}", period, timeUnit,
            Thread.currentThread().getId());
        // do not execute immediately. Use period also for initial delay.
        final long initialDelay = period;
        future = poolInitializer.get().scheduleWithFixedDelay(runnable, initialDelay, period, timeUnit);
      }
    }
  }

  /**
   * The following method shuts down an ExecutorService in two phases, first by calling shutdown to reject incoming
   * tasks, and then calling shutdownNow, if necessary, to cancel any lingering tasks.
   */
  private synchronized void destroyScheduler() {
    if (!poolInitializer.get().isShutdown()) {
      // Disable new tasks from being submitted
      poolInitializer.get().shutdown();
      if (future != null) {
        future.cancel(true);
      }
      try {
        while (!poolInitializer.get().awaitTermination(5, TimeUnit.SECONDS)) {
          LOG.debug("Termination awaited: {}", name);
          poolInitializer.get().shutdownNow();
        }
      } catch (final InterruptedException e) {
        LOG.debug("Interrupted Exception occured during scheduler destroy", e);
        // (Re-)Cancel if current thread also interrupted
        poolInitializer.get().shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      } finally {
        LOG.debug("[STOP] Scheduler terminated successfully! {}", name);
      }
    }
  }

  private void cancelRunningTask() {
    if (future != null) {
      future.cancel(false);
      LOG.debug("[STOP] Scheduler terminated successfully! {}", name);
    }
  }

  /**
   * Stops all jobs runned by the scheduler. It is important to call this method before application stops.
   */
  public void destroy() {
    destroyScheduler();
  }

  /**
   * @return the period
   */
  long getPeriod() {
    return period;
  }
}
