package ro.isdc.wro.extensions.processor.js;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Similar to {@link RhinoCoffeeScriptProcessor} but will prefer using {@link NodeCoffeeScriptProcessor} if it is
 * supported and will fallback to rhino based processor.<br/>
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
@SupportedResourceType(ResourceType.JS)
public class CoffeeScriptProcessor
    extends AbstractNodeWithFallbackProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CoffeeScriptProcessor.class);
  public static final String ALIAS = "coffeeScript";

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createNodeProcessor() {
    LOG.debug("creating NodeCoffeeScript processor");
    return new NodeCoffeeScriptProcessor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createFallbackProcessor() {
    LOG.debug("Node CoffeeScript is not supported. Using fallback Rhino processor");
    return new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoCoffeeScriptProcessor();
      }
    });
  }
}
