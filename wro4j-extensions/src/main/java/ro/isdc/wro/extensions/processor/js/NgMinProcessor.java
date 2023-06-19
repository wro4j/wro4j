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
 * <p>Uses ngmin node utility.</p>
 *
 * <p>Ngmin is a "pre-minifier" for AngularJS. It modifies Angular code to prevent errors that may arise from minification.</p>
 *
 * <p>More details can be found here: http://www.thinkster.io/pick/XlWneEZCqY/angularjs-ngmin</p>
 *
 * @author Janek L.B
 * @since 1.7.4
 */
@SupportedResourceType(ResourceType.JS)
public class NgMinProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, SupportAware {

  public static final String ALIAS = "ngMin";
  private static final Logger LOG = LoggerFactory.getLogger(NgMinProcessor.class);

  /**
   * 'ngmin' must be available on the $PATH, and can be installed via npm: 'npm install -g ngmin'
   */
  private static final String NGMIN_COMMAND = "ngmin";

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
      throws ExecuteException, IOException {
    final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    final Executor executor = new DefaultExecutor();
    executor.setStreamHandler(new PumpStreamHandler(out, errorStream, in));
    final int result = executor.execute(CommandLine.parse(NGMIN_COMMAND));
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
