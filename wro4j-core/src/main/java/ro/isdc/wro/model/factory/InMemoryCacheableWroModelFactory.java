/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.util.StopWatch;


/**
 * Adds the ability to cache the model in memory. This class replace the schedulerAwareDecorator, because the scheduler
 * job is not done in decorator anymore, but rather in {@link WroManager}.
 *
 * @author Alex Objelean
 * @created 15 Oct 2011
 * @since 1.4.2
 */
public class InMemoryCacheableWroModelFactory extends WroModelFactoryDecorator {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(InMemoryCacheableWroModelFactory.class);
  /**
   * Reference to cached model instance. Using volatile keyword fix the problem with double-checked locking in JDK 1.5.
   */
  private volatile WroModel model;

  public InMemoryCacheableWroModelFactory(final WroModelFactory decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
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
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    model = null;
  }
}
