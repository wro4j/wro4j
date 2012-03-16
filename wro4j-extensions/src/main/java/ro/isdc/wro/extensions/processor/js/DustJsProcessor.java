package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.dustjs.DustJs;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * A processor for dustJs template framework. Uses <a href="http://akdubya.github.com/dustjs/">dustjs</a> library to
 * transform a template into plain javascript.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 * @created 8 Mar 2012
 */
public class DustJsProcessor implements ResourceProcessor {
  public static final String ALIAS = "DustJs";

  private final ObjectPoolHelper<DustJs> enginePool;

  public DustJsProcessor() {
    enginePool = new ObjectPoolHelper<DustJs>(new ObjectFactory<DustJs>() {
      @Override
      public DustJs create() {
        return new DustJs();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
    final String content = IOUtils.toString(reader);
    final DustJs dustJs = enginePool.getObject();
    final String name = resource == null ? "" : FilenameUtils.getBaseName(resource.getUri());
    try {
      writer.write(dustJs.compile(content, name));
    } finally {
      enginePool.returnObject(dustJs);
      reader.close();
      writer.close();
    }
  }
}
