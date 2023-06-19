package ro.isdc.wro.extensions.processor.css;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.js.AbstractNodeWithFallbackProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Similar to {@link RhinoLessCssProcessor} but will prefer using {@link NodeLessCssProcessor} if it is supported and
 * will fallback to rhino based processor.<br/>
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
@SupportedResourceType(ResourceType.CSS)
public class LessCssProcessor
    extends AbstractNodeWithFallbackProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(LessCssProcessor.class);
  public static final String ALIAS = "lessCss";

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createNodeProcessor() {
    LOG.debug("creating NodeLess processor");
    return new NodeLessCssProcessor();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createFallbackProcessor() {
    LOG.debug("NodeLess is not supported. Using fallback RhinoLess processor");
    return new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoLessCssProcessor();
      }
    });
  }
}
