package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;


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
    implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractNodeWithFallbackProcessor.class);
  @Inject
  private Injector injector;
  private ResourceProcessor processor;

  /**
   * Responsible for coffeeScriptProcessor initialization. First the nodeCoffeeScript processor will be used as a
   * primary processor. If it is not supported, the fallback processor will be used.
   */
  private ResourceProcessor initializeProcessor() {
    final ProcessorDecorator processor = new ProcessorDecorator(createNodeProcessor());
    return processor.isSupported() ? processor : createFallbackProcessor();
  }

  /**
   * @return {@link ResourcePreProcessor} used as a primary processor.
   * @VisibleForTesting
   */
  protected abstract ResourceProcessor createNodeProcessor();

  /**
   * {@inheritDoc}
   */
  @Override
  public final void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    getProcessor().process(resource, reader, writer);
  }

  private ResourceProcessor getProcessor() {
    if (processor == null) {
      processor = initializeProcessor();
      LOG.debug("initialized processor: {}", processor);
      injector.inject(processor);
    }
    return processor;
  }

  /**
   * Lazily initialize the rhinoProcessor.
   *
   * @return {@link ResourcePreProcessor} used as a fallback processor.
   * @VisibleFortesTesting
   */
  protected abstract ResourceProcessor createFallbackProcessor();
}
