/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.SchedulerHelper;
import ro.isdc.wro.util.StopWatch;


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
  private final SchedulerHelper schedulerHelper;

  public ScheduledWroModelFactory(final WroModelFactory decorated) {
    super(decorated);
    schedulerHelper = SchedulerHelper.create(new ObjectFactory<Runnable>() {
      public Runnable create() {
        return getSchedulerRunnable();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    final long period = Context.get().getConfig().getModelUpdatePeriod();
    schedulerHelper.scheduleWithPeriod(period);

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

    final long period = Context.get().getConfig().getModelUpdatePeriod();
    schedulerHelper.scheduleWithPeriod(period);
    // force scheduler to reload
    model = null;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    // kill running threads
    schedulerHelper.destroy();
  }

}
