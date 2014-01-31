package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.util.DestroyableLazyInitializer;


/**
 * An abstract processor which should be extended by processors which can provide both: node & a fallback (usually
 * rhino) implementation of processor. The node version of processor is preferred, but if unavailable - the fallback
 * will be used.
 *
 * @author Alex Objelean
 * @since 1.6.3
 * @created 21 Jan 2013
 */
public abstract class AbstractNodeWithFallbackProcessor
    implements ResourceProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeWithFallbackProcessor.class);
  @Inject
  private Injector injector;
  private final DestroyableLazyInitializer<ResourceProcessor> processorInitializer = new DestroyableLazyInitializer<ResourceProcessor>() {
    @Override
    protected ResourceProcessor initialize() {
      /**
       * Responsible for node processor initialization. First the nodeCoffeeScript processor will be used as a primary
       * processor. If it is not supported, the fallback processor will be used.
       */
      processor = createNodeProcessor();
      processor = new ProcessorDecorator(processor).isSupported() ? processor : createFallbackProcessor();
      injector.inject(processor);
      return processor;
    }

    @Override
    public void destroy() {
      if (isInitialized()) {
        try {
          new ProcessorDecorator(get()).destroy();
        } catch (final Exception e) {
          LOG.error("Exception while destroying processor", e);
          WroRuntimeException.wrap(e);
        }
      }
      super.destroy();
    };
  };
  private ResourceProcessor processor;

  /**
   * @return {@link ResourcePreProcessor} used as a primary processor.
   * @VisibleForTesting
   */
  protected abstract ResourceProcessor createNodeProcessor();

  @Override
  public final void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    processorInitializer.get().process(resource, reader, writer);
  }

  /*
   * @return {@link ResourcePreProcessor} used as a fallback processor.
   * @VisibleFortesTesting
   */
  protected abstract ResourceProcessor createFallbackProcessor();

  @Override
  public void destroy()
      throws Exception {
    processorInitializer.destroy();
  }
}
