package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;


/**
 * Yet another processor which compiles less to css. This implementation uses open source java library called less4j.
 *
 * @author Alex Objelean
 * @since 1.6.0
 * @created 5 Oct 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class Less4jProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(Less4jProcessor.class);

  public static final String ALIAS = "less4j";
  private final LessCompiler compiler = new DefaultLessCompiler();
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    try {
      final LessCompiler.CompilationResult result =compiler.compile(IOUtils.toString(reader));
      writer.write(result.getCss());
    } catch (final Exception e) {
      LOG.error("Failed to compile less", e);
      throw WroRuntimeException.wrap(e);
    }
  }
}
