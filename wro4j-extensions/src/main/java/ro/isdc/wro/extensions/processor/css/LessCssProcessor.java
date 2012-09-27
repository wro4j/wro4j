package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Similar to {@link RhinoLessCssProcessor} but will prefer using {@link NodeLessCssProcessor} if it is supported and will
 * fallback to rhino based processor.
 * 
 * @author Alex Objelean
 * @since 1.5.0
 * @created 11 Sep 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class LessCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(LessCssProcessor.class);
  public static final String ALIAS = "lessCss";
  private final ResourcePreProcessor lessProcessor;
  
  public LessCssProcessor() {
    lessProcessor = initializeProcessor();
  }

  /**
   * Responsible for lessProcessor initialization. First the nodeLess processor will be used as a primary processor. If
   * it is not supported, the fallback processor will be used.
   */
  private ResourcePreProcessor initializeProcessor() {
    final ProcessorDecorator processor = new ProcessorDecorator(createNodeProcessor());
    return processor.isSupported() ? processor : createRhinoProcessor();
  }

  /**
   * @return {@link NodeLessCssProcessor} used as a primary LessProcessor. 
   * @VisibleForTesting
   */
  protected NodeLessCssProcessor createNodeProcessor() {
    LOG.debug("creating NodeLess processor");
    return new NodeLessCssProcessor();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    lessProcessor.process(resource, reader, writer);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
  
  /**
   * Lazily initialize the rhinoProcessor.
   * @return {@link ResourcePostProcessor} used as a fallback lessCss processor.
   * @VisibleFortesTesting
   */
  protected ResourcePreProcessor createRhinoProcessor() {
    LOG.debug("NodeLess is not supported. Using fallback RhinoLess processor");
    return new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
      @Override
      protected ResourcePreProcessor initialize() {
        return new RhinoLessCssProcessor();
      }
    });
  }
}
