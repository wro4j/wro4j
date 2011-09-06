/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.StopWatch;


/**
 * Decorator for {@link ProcessorsFactory} responsible for processing @Inject annotations of processors provided by
 * decorated factory.
 *
 * @author Alex Objelean
 * @created 21 Nov 2010
 */
public class InjectorProcessorsFactoryDecorator
  extends ProcessorsFactoryDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(InjectorProcessorsFactoryDecorator.class);
  private final Injector injector;

  public InjectorProcessorsFactoryDecorator(final ProcessorsFactory decorated, final Injector injector) {
    super(decorated);
    Validate.notNull(injector);
    this.injector = injector;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ResourceProcessor> getPreProcessors() {
    scanPreProcessors();
    return super.getPreProcessors();
  }


  /**
   * Scan all preProcessors of decorated factory.
   */
  private void scanPreProcessors() {
    LOG.debug("scanPreProcessors");
    // TODO ensure that it is not called to often
    for (final ResourceProcessor processor : super.getPreProcessors()) {
      injector.inject(processor);
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ResourceProcessor> getPostProcessors() {
    scanPostProcessors();
    return super.getPostProcessors();
  }


  private void scanPostProcessors() {
    LOG.debug("scanPostProcessors");
    final StopWatch watch = new StopWatch();
    watch.start("scan post processors");
    // TODO ensure that it is not called to often
    for (final ResourceProcessor processor : super.getPostProcessors()) {
      injector.inject(processor);
    }
    watch.stop();
    LOG.debug(watch.prettyPrint());
  }
}
