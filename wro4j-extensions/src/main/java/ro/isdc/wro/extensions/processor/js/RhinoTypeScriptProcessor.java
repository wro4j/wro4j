/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.RhinoLessCssProcessor;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.typescript.TypeScriptCompiler;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Compiles TypeScript into javascript in a cross platform manner. Uses rhino to interpret javascript implementation of
 * the compiler.
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
@SupportedResourceType(ResourceType.JS)
public class RhinoTypeScriptProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(RhinoLessCssProcessor.class);

  public static final String ALIAS = "rhinoTypeScript";

  private ObjectPoolHelper<TypeScriptCompiler> enginePool;

  public RhinoTypeScriptProcessor() {
    enginePool = new ObjectPoolHelper<TypeScriptCompiler>(new ObjectFactory<TypeScriptCompiler>() {
      @Override
      public TypeScriptCompiler create() {
        return newCompiler();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    LOG.debug("processing resource: {}", resource);
    final String content = IOUtils.toString(reader);
    final TypeScriptCompiler compiler = enginePool.getObject();
    try {
      writer.write(compiler.compile(content));
    } catch (final Exception e) {
      onException(e, content);
    } finally {
      // return for later reuse
      enginePool.returnObject(compiler);
      reader.close();
      writer.close();
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final Exception e, final String content) {
    throw WroRuntimeException.wrap(e);
  }

  /**
   * @return the {@link TypeScriptCompiler} engine implementation. Override it to provide a different version of the typescript.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected TypeScriptCompiler newCompiler() {
    return new TypeScriptCompiler();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  @Override
  public void destroy() throws Exception {
    enginePool.destroy();
  }
}
