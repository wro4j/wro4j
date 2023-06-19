package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

/**
 * A base class for template processors like: dustJS or hoganJS.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.7
 */
public abstract class JsTemplateCompilerProcessor implements ResourcePreProcessor, Destroyable {
  private final ObjectPoolHelper<AbstractJsTemplateCompiler> enginePool;

  public JsTemplateCompilerProcessor() {
    enginePool = new ObjectPoolHelper<AbstractJsTemplateCompiler>(new ObjectFactory<AbstractJsTemplateCompiler>() {
      @Override
      public AbstractJsTemplateCompiler create() {
        return createCompiler();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
    final String content = IOUtils.toString(reader);
    final AbstractJsTemplateCompiler jsCompiler = enginePool.getObject();
    try {
      writer.write(jsCompiler.compile(content, getArgument(resource)));
    } finally {
      enginePool.returnObject(jsCompiler);
      reader.close();
      writer.close();
    }
  }

  /**
   * @param resource
   *          {@link Resource} being processed by compiler.
   * @return arguments consumed by the js compile script.
   */
  protected String getArgument(final Resource resource) {
    return resource == null ? "" : FilenameUtils.getBaseName(resource.getUri());
  }

  /**
   * @return the {@link AbstractJsTemplateCompiler} responsible for compiling the template.
   */
  protected abstract AbstractJsTemplateCompiler createCompiler();

  @Override
  public void destroy() throws Exception {
    enginePool.destroy();
  }
}
