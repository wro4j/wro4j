/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.algorithm.JawrCssMinifier;


/**
 * A processor implementation   using {@link JawrCssMinifier} algorithm. This processor can be used as both: PreProcessor
 * & postProcessor. <br/>
 * This processor is annotated with {@link Minimize} because it performs minimization.
 *
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.CSS)
public class JawrCssMinifierProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    process(reader, writer);
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      final StringBuffer result = new JawrCssMinifier().minifyCSS(new StringBuffer(content));
      writer.write(result.toString());
      writer.flush();
    } catch (final Exception e) {
      throw new IOException("Exception occured while minimizing the css");
    } finally {
      reader.close();
      writer.close();
    }
  }
}
