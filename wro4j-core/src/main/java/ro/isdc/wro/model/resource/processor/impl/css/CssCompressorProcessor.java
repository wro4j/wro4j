/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.support.CssCompressor;


/**
 * A processor implementation using {@link CssCompressor} algorithm. This processor can be used as both: PreProcessor &
 * postProcessor.<br/>
 * This processor is annotated with {@link Minimize} because it performs minimization.
 *
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.CSS)
public class CssCompressorProcessor
  implements ResourceProcessor {
  public static final String ALIAS = "cssCompressor";

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      new CssCompressor(reader).compress(writer, -1);
      writer.flush();
    } catch (final Exception e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      throw new IOException("Exception while applying " + getClass().getSimpleName() + " processor on the "
          + resourceUri + " resource", e);
    } finally {
      reader.close();
      writer.close();
    }
  }
}
