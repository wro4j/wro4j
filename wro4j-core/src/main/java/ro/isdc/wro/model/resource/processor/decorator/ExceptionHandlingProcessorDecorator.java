package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;

/**
 * Responsible for handling exception thrown by decorated processor. If the processing fails, the behavior will vary based on the {@link WroConfiguration#isIgnoreFailingProcessor()} flag:
 * <ul>
 * <li>When the flag is false (default) - the exception is wrapped in {@link WroRuntimeException} and thrown further</li>
 * <li>When the flag is true - the exception is ignored and the writer will get the unchanged content from the reader. </li>
 * </ul>
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class ExceptionHandlingProcessorDecorator
    extends ProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlingProcessorDecorator.class);
  @Inject
  private ReadOnlyContext context;

  /**
   * Decorates a processor with failure handling ability.
   *
   * @param processor
   *          to decorate.
   */
  public ExceptionHandlingProcessorDecorator(final Object processor) {
    super(processor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String resourceContent = IOUtils.toString(reader);
    final Reader innerReader = new StringReader(resourceContent);
    final StringWriter innerWriter = new StringWriter();
    try {
      super.process(resource, innerReader, innerWriter);
      writer.write(innerWriter.toString());
    } catch (final Exception e) {
      final String processorName = toString();
      if (isIgnoreFailingProcessor()) {
        LOG.debug("Ignoring failed processor. Original Exception", e);
        writer.write(resourceContent);
        // don't wrap exception unless required
      } else {
        LOG.error("Failed to process the resource: {} using processor: {}. Reason: {}", resource, processorName, e.getMessage());
        final String resourceUri = resource != null ? resource.getUri() : null;
        throw WroRuntimeException.wrap(e,
            "The processor: " + processorName + " faile while processing uri: " + resourceUri).setResource(resource);
      }
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * @return true if the failure should be ignored. By default uses the {@link WroConfiguration} to get the flag value.
   */
  protected boolean isIgnoreFailingProcessor() {
    return context.getConfig().isIgnoreFailingProcessor();
  }
}
