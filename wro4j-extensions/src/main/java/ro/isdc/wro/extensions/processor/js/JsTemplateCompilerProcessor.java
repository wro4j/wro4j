package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.support.JsTemplateCompiler;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public abstract class JsTemplateCompilerProcessor implements ResourcePreProcessor {
  private final ObjectPoolHelper<JsTemplateCompiler> enginePool;

  public JsTemplateCompilerProcessor() {
    enginePool = new ObjectPoolHelper<JsTemplateCompiler>(new ObjectFactory<JsTemplateCompiler>() {
      @Override
      public JsTemplateCompiler create() {
        return createCompiler();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(Resource resource, Reader reader, Writer writer) throws IOException {
    final String content = IOUtils.toString(reader);
    final JsTemplateCompiler jsCompiler = enginePool.getObject();
    try {
      writer.write(jsCompiler.compile(content, getArgument(resource)));
    } finally {
      enginePool.returnObject(jsCompiler);
      reader.close();
      writer.close();
    }
  }

  protected String getArgument(Resource resource) {
    return null;
  }

  protected abstract JsTemplateCompiler createCompiler();
}
