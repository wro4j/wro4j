/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Decorates the {@link WroModelFactory} with a scheduler ability.
 *
 * @author Alex Objelean
 */
public class ScheduledWroModelFactory extends WroModelFactoryDecorator {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ScheduledWroModelFactory.class);
  /**
   * Reference to cached model instance. Using volatile keyword fix the problem with double-checked locking in JDK 1.5.
   */
  private volatile WroModel model;
  /**
   * Scheduled executors service, used to refresh the WroModel.
   */
  private volatile ScheduledExecutorService scheduler;

  public ScheduledWroModelFactory(final WroModelFactory decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    initScheduler();
    // use double-check locking
    if (model == null) {
      synchronized (this) {
        if (model == null) {
          final StopWatch stopWatch = new StopWatch();
          stopWatch.start("Create Model");
          model = super.create();
          stopWatch.stop();
          LOG.debug(stopWatch.prettyPrint());
        }
      }
    }
    return model;
  }

  /**
   * Initialize executor service & start the thread responsible for updating the model.
   */
  private void initScheduler() {
    if (scheduler == null) {
      synchronized (this) {
        if (scheduler == null) {
          final long period = Context.get().getConfig().getModelUpdatePeriod();
          if (period > 0) {
            scheduler = Executors.newSingleThreadScheduledExecutor(WroUtil.createDaemonThreadFactory());
            // Run a scheduled task which updates the model.
            // Here a scheduleWithFixedDelay is used instead of scheduleAtFixedRate because the later can cause a problem
            // (thread tries to make up for lost time in some situations)
            LOG.info("Schedule Model Update for " + period + " seconds period");
            scheduler.scheduleWithFixedDelay(getSchedulerRunnable(), 0, period, TimeUnit.SECONDS);
          }
        }
      }
    }
  }

  /**
   * @return {@link Runnable} implementation which reloads the model when scheduled.
   */
  private Runnable getSchedulerRunnable() {
    return new Runnable() {
      public void run() {
        try {
          model = ScheduledWroModelFactory.super.create();
          // find a way to clear the cache
          LOG.info("Wro Model (wro.xml) updated!");
        } catch (final Exception e) {
          LOG.error("Exception occured", e);
        }
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onModelPeriodChanged() {
    LOG.debug("notified about model change");
    super.onModelPeriodChanged();
    if (scheduler != null) {
      synchronized (this) {
        if (scheduler != null) {
          scheduler.shutdown();
          scheduler = null;
        }
      }
    }
    // force scheduler to reload
    model = null;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    // kill running threads
    if (scheduler != null) {
      scheduler.shutdownNow();
    }
  }

}
