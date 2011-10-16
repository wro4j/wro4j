/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.util;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
  private ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor(createDaemonThreadFactory());
  /**
   * A factory providing the runnable to schedule.
   */
  private final ObjectFactory<Runnable> runnableFactory;
  /**
   * Period in seconds (how often a runnable should run).
   */
  private volatile long period = 0;

  private String name;
  private Future<?> future;


  private SchedulerHelper(final ObjectFactory<Runnable> runnableFactory) {
    this(runnableFactory, null);
  }


  private SchedulerHelper(final ObjectFactory<Runnable> runnableFactory, final String name) {
    Validate.notNull(runnableFactory);
    this.name = name;
    this.runnableFactory = runnableFactory;
  }


  /**
   * Factory method. Creates a {@link SchedulerHelper} which consumes a factory providing a runnable. This approach
   * allows lazy runnable initialization.
   *
   * @param runnableFactory a factory creating the runnable to schedule.
   * @param name the name associated with this {@link SchedulerHelper} (useful to detect if this class is causing a
   *        memory leak.
   */
  public static SchedulerHelper create(final ObjectFactory<Runnable> runnableFactory, final String name) {
    return new SchedulerHelper(runnableFactory, name);
  }


  /**
   * @see SchedulerHelper#create(ObjectFactory, String)
   */
  public static SchedulerHelper create(final ObjectFactory<Runnable> runnableFactory) {
    LOG.info("creating SchedulerHelper");
    return new SchedulerHelper(runnableFactory);
  }


  /**
   * Run the scheduler with the provided period of time. If the scheduler is already started, it will be stopped (not
   * before the running job is complete).
   *
   * @param period new period for scheduling.
   * @param timeUnit what kind of time unit is associated with the period.
   */
  public SchedulerHelper scheduleWithPeriod(final long period, final TimeUnit timeUnit) {
    Validate.notNull(timeUnit);
    LOG.debug("period: {}", period);
    LOG.debug("timeUnit: {}", timeUnit);
    if (this.period != period) {
      this.period = period;
      if (!pool.isShutdown()) {
        startScheduler(period, timeUnit);
      } else {
        LOG.warn("Cannot schedule because destroy was already called!");
      }
    }
    return this;
  }


  /**
   * @param period
   * @param timeUnit
   */
  private synchronized void startScheduler(final long period, final TimeUnit timeUnit) {
    // let the running task to finish its job.
    cancelRunningTask();
    if (period > 0) {
      // scheduleWithFixedDelay is used instead of scheduleAtFixedRate because the later can cause a problem
      // (thread tries to make up for lost time in some situations)
      final Runnable runnable = runnableFactory.create();
      Validate.notNull(runnable);
      // avoid reject when this method is accessed concurrently.
      if (!pool.isShutdown()) {
        LOG.debug("\t[START] Scheduling thread with period of {} - {}", period, Thread.currentThread().getId());
        future = pool.scheduleWithFixedDelay(decorate(runnable), 0, period, timeUnit);
      }
    }
  }


  private void cancelRunningTask() {
    if (future != null) {
      future.cancel(false);
      LOG.debug("[STOP] Scheduler terminated successfully! {}", Thread.currentThread().getId());
    }
  }


  public static Runnable decorate(final Runnable runnable) {
    final long threadId = Thread.currentThread().getId();
    return new Runnable() {
      public void run() {
        LOG.debug("\tThreadId: {}", threadId);
        runnable.run();
      }
    };
  }


  /**
   * Schedules with provided period using {@link TimeUnit#SECONDS} as a default time unit.
   *
   * @param period new period for scheduling.
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
   * The following method shuts down an ExecutorService in two phases, first by calling shutdown to reject incoming
   * tasks, and then calling shutdownNow, if necessary, to cancel any lingering tasks:
   *
   * @param destroyNow - if true, any running operation will be stopped immediately, otherwise scheduler will await
   *        termination.
   */
  private synchronized void destroyScheduler() {
    LOG.info("destroyScheduler: {} with id {}", this.name, Thread.currentThread().getId());
    if (!pool.isShutdown()) {
      pool.shutdownNow(); // Disable new tasks from being submitted
      try {
        // Wait a while for existing tasks to terminate
        if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
          // Cancel currently executing tasks
          pool.shutdownNow();
          // Wait a while for tasks to respond to being cancelled
          if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            LOG.error("Pool did not terminate");
          }
        }
      } catch (final InterruptedException e) {
        // (Re-)Cancel if current thread also interrupted
        pool.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      } finally {
        LOG.info("[STOP] Scheduler terminated successfully! {}", Thread.currentThread().getId());
      }

//      scheduler.shutdownNow();
//      // make sure scheduler is terminated before continue thread execution in order to prevent memory leak
//      // (reported by tomcat).
//      boolean terminated = future == null || future != null && future.isDone();
//      while (!terminated) {
//        try {
//          LOG.info("\tawaiting scheduler termination...");
//          LOG.info("\tdetails: ... s.shutDown: " + scheduler.isShutdown() + " s.terminated: "
//            + scheduler.isTerminated() + " f.done: " + future.isDone() + " f.cancelled: " + future.isCancelled());
//          terminated = scheduler.awaitTermination(500, TimeUnit.MILLISECONDS);
//          Thread.sleep(500);
//          terminated = future.cancel(true);
//        } catch (final InterruptedException e) {
//          LOG.info("couldn't await scheduler termination", e);
//          break;
//        }
//      }
    }
  }


  /**
   * @return {@link ThreadFactory} with daemon threads.
   */
  private static ThreadFactory createDaemonThreadFactory() {
    return new ThreadFactory() {
      public Thread newThread(final Runnable runnable) {
        final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName("wro4j-daemon-thread");
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
