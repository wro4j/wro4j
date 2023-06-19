package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.util.DestroyableLazyInitializer;


/**
 * An abstract processor which should be extended by processors which can provide both: node and a fallback (usually
 * rhino) implementation of processor. The node version of processor is preferred, but if unavailable - the fallback
 * will be used.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public abstract class AbstractNodeWithFallbackProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  @Inject
  private Injector injector;
  private final DestroyableLazyInitializer<ResourcePreProcessor> processorInitializer = new DestroyableLazyInitializer<ResourcePreProcessor>() {
    @Override
    protected ResourcePreProcessor initialize() {
      /**
       * Responsible for node processor initialization. First the nodeCoffeeScript processor will be used as a
       * primary processor. If it is not supported, the fallback processor will be used.
       */
      processor = createNodeProcessor();
      processor = new ProcessorDecorator(processor).isSupported() ? processor: createFallbackProcessor();
      injector.inject(processor);
      return processor;
    }
  };
  private ResourcePreProcessor processor;


  /**
   * @return {@link ResourcePreProcessor} used as a primary processor.
   */
  protected abstract ResourcePreProcessor createNodeProcessor();

  @Override
  public final void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    processorInitializer.get().process(resource, reader, writer);
  }

  @Override
  public final void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  /**
   * Factory method for creating a fallback processor.
   *
   * @return {@link ResourcePreProcessor} used as a fallback processor.
   */
  protected abstract ResourcePreProcessor createFallbackProcessor();

  @Override
  public void destroy()
      throws Exception {
    processorInitializer.destroy();
  }
}
