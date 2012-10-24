package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Similar to {@link RhinoCoffeeScriptProcessor} but will prefer using {@link NodeCoffeeScriptProcessor} if it is supported and
 * will fallback to rhino based processor.<br/>
 *
 * @author Alex Objelean
 * @since 1.6.0
 * @created 11 Sep 2012
 */
@SupportedResourceType(ResourceType.JS)
public class CoffeeScriptProcessor
    implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CoffeeScriptProcessor.class);
  public static final String ALIAS = "coffeeScript";
  private final ResourceProcessor processor;

  public CoffeeScriptProcessor() {
    processor = initializeProcessor();
  }

  /**
   * Responsible for coffeeScriptProcessor initialization. First the nodeCoffeeScript processor will be used as a primary processor. If
   * it is not supported, the fallback processor will be used.
   */
  private ResourceProcessor initializeProcessor() {
    final ProcessorDecorator processor = new ProcessorDecorator(createNodeProcessor());
    return processor.isSupported() ? processor : createRhinoProcessor();
  }

  /**
   * @return {@link ResourceProcessor} used as a primary processor.
   * @VisibleForTesting
   */
  ResourceProcessor createNodeProcessor() {
    LOG.debug("creating NodeCoffeeScript processor");
    return new NodeCoffeeScriptProcessor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    processor.process(resource, reader, writer);
  }

  /**
   * Lazily initialize the rhinoProcessor.
   *
   * @return {@link ResourceProcessor} used as a fallback processor.
   * @VisibleFortesTesting
   */
  ResourceProcessor createRhinoProcessor() {
    LOG.debug("Node CoffeeScript is not supported. Using fallback Rhino processor");
    return new LazyProcessorDecorator(new LazyInitializer<ResourceProcessor>() {
      @Override
      protected ResourceProcessor initialize() {
        return new RhinoCoffeeScriptProcessor();
      }
    });
  }
}
