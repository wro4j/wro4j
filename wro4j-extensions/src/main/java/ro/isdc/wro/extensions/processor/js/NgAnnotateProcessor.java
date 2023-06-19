package ro.isdc.wro.extensions.processor.js;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;


/**
 * Uses ng-annotate node utility. Adds and removes AngularJS dependency injection annotations. More details can be found
 * here: https://github.com/olov/ng-annotate
 *
 * @author Manuel S. (adapted from Janek L.B's NgMinProcessor)
 * @since 1.7.8
 */
@SupportedResourceType(ResourceType.JS)
public class NgAnnotateProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, SupportAware {

  public static final String ALIAS = "ngAnnotate";
  private static final Logger LOG = LoggerFactory.getLogger(NgAnnotateProcessor.class);

  /**
   * 'ng-annotate' must be available on the $PATH, and can be installed via npm: 'npm install -g ng-annotate'
   */
  private static final String NGANN_COMMAND = "ng-annotate --single_quotes -a -";

  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final OutputStream out = new WriterOutputStream(writer);
    final InputStream in = new ReaderInputStream(reader);

    try {
      doProcess(in, out);
    } catch (final IOException ex) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", ex);
      throw ex;
    }
  }

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  void doProcess(final InputStream in, final OutputStream out)
      throws IOException {
    final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    final Executor executor = new DefaultExecutor();
    executor.setStreamHandler(new PumpStreamHandler(out, errorStream, in));
    final int result = executor.execute(CommandLine.parse(NGANN_COMMAND));
    LOG.debug("result={}", result);
    if (result != 0) {
      throw new ExecuteException("Processing failed: " + new String(errorStream.toByteArray()), result);
    }
  }

  @Override
  public boolean isSupported() {
    boolean supported = true;

    try (InputStream input = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))) {
      doProcess(input, null);
    } catch (final Exception e) {
      LOG.debug("The {} processor is not supported. Because: {}", getClass().getName(), e.getMessage());
      supported = false;
    }

    return supported;
  }

}
