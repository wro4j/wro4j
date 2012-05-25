/**
 *
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.handlebarsjs.HandlebarsJs;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * This {@link ResourcePreProcessor} compiles HandlebarsJS templates to javascript.
 *
 * @author heldeen
 */
public class HandlebarsJsProcessor
    implements ResourcePreProcessor {
  public static final String ALIAS = "HandlebarsJs";

  private final ObjectPoolHelper<HandlebarsJs> enginePool;

  public HandlebarsJsProcessor() {
    enginePool = new ObjectPoolHelper<HandlebarsJs>(new ObjectFactory<HandlebarsJs>() {

      @Override
      public HandlebarsJs create() {
        return new HandlebarsJs();
      }

    });
  }

  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {

    final String content = IOUtils.toString(reader);
    final HandlebarsJs handlebarsJs = enginePool.getObject();
    final String name = resource == null ? "" : FilenameUtils.getBaseName(resource.getUri());

    try {
      writer.write(handlebarsJs.compile(content, name));
    } finally {
      enginePool.returnObject(handlebarsJs);
      reader.close();
      writer.close();
    }
  }

  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

}
