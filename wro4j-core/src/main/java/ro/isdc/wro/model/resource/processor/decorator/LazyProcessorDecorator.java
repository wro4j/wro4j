package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessorAware;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Decorates a {@link LazyInitializer} which creates a processor. Allows defer the initialization of the decorated
 * processor, making it unnecessary dependency unless it is really used.
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public final class LazyProcessorDecorator
    extends AbstractDecorator<LazyInitializer<ResourcePreProcessor>>
    implements ResourceProcessorAware {
  @Inject
  private Injector injector;
  private ProcessorDecorator processor;


  public LazyProcessorDecorator(final LazyInitializer<ResourcePreProcessor> processor) {
    super(processor);
  }

  private ProcessorDecorator getProcessorDecorator() {
    if (processor == null) {
      processor = new ProcessorDecorator(getDecoratedObject().get());
      injector.inject(processor);
    }
    return processor;
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    getProcessorDecorator().process(resource, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    getProcessorDecorator().process(reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public SupportedResourceType getSupportedResourceType() {
    return getProcessorDecorator().getSupportedResourceType();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isMinimize() {
    return getProcessorDecorator().isMinimize();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isSupported() {
    return getProcessorDecorator().isSupported();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isImportAware() {
    return getProcessorDecorator().isImportAware();
  }

  /**
   * {@inheritDoc}
   */
  public void destroy()
      throws Exception {
    getProcessorDecorator().destroy();
  }

  @Override
  public String toString() {
    //Handle situation when toString is invoked before this instance is injected.
    return injector != null ? getProcessorDecorator().toString() : super.toString();
  }
}
