package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.hoganjs.HoganJs;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A processor for hogan.js template framework. Uses <a href="http://twitter.github.com/hogan.js/">hogan.js</a> library to
 * transform a template into plain javascript.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 * @created 8 Mar 2012
 */
public class HoganJsProcessor implements ResourcePreProcessor {
  public static final String ALIAS = "hoganJs";

  private final ObjectPoolHelper<HoganJs> enginePool;

  public HoganJsProcessor() {
    enginePool = new ObjectPoolHelper<HoganJs>(new ObjectFactory<HoganJs>() {
      @Override
      public HoganJs create() {
        return new HoganJs();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(Resource resource, Reader reader, Writer writer) throws IOException {
    final String content = IOUtils.toString(reader);
    final HoganJs hoganJs = enginePool.getObject();
    try {
      writer.write(hoganJs.compile(content, null));
    } finally {
      enginePool.returnObject(hoganJs);
      reader.close();
      writer.close();
    }
  }
}
