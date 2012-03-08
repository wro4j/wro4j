package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.dustjs.DustJs;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class DustJsProcessor implements ResourcePreProcessor, ResourcePostProcessor {
  private ObjectPoolHelper<DustJs> enginePool;

  public DustJsProcessor() {
    enginePool = new ObjectPoolHelper<DustJs>(new ObjectFactory<DustJs>() {
      @Override
      public DustJs create() {
        return new DustJs();
      }
    });
  }

  @Override
  public void process(Reader reader, Writer writer) throws IOException {
    process(null, reader, writer);
  }

  @Override
  public void process(Resource resource, Reader reader, Writer writer) throws IOException {
    final String content = IOUtils.toString(reader);
    final DustJs dustJs = enginePool.getObject();
    final String name = resource == null ? "" : getFileName(resource.getUri());
    try {
      writer.write(dustJs.compile(content, name));
    } finally {
      reader.close();
      writer.close();
      enginePool.returnObject(dustJs);
    }
  }

  private String getFileName(String uri) {
    int firstIndex = uri.lastIndexOf('/') + 1;
    int lastIndex = uri.lastIndexOf('.');
    if(firstIndex > lastIndex) {
      return uri.substring(firstIndex);
    } else {
      return uri.substring(firstIndex, lastIndex);
    }
  }
}
