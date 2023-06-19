/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.coffeescript.CoffeeScript;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;



/**
 * Uses coffee script library loaded from the webjar to compile to javascript code.
 *
 * @author Alex Objelean
 * @since 1.3.6
 */
@SupportedResourceType(ResourceType.JS)
public class RhinoCoffeeScriptProcessor
  implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(RhinoCoffeeScriptProcessor.class);
  public static final String ALIAS = "rhinoCoffeeScript";
  private ObjectPoolHelper<CoffeeScript> enginePool;


  public RhinoCoffeeScriptProcessor() {
    enginePool = new ObjectPoolHelper<CoffeeScript>(new ObjectFactory<CoffeeScript>() {
      @Override
      public CoffeeScript create() {
        return newCoffeeScript();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    final CoffeeScript coffeeScript = enginePool.getObject();
    try {
      writer.write(coffeeScript.compile(content));
    } catch (final Exception e) {
      onException(e);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.error("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
      enginePool.returnObject(coffeeScript);
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final Exception e) {
    throw WroRuntimeException.wrap(e);
  }

  /**
   * @return the {@link CoffeeScript} engine implementation. Override it to provide a different version of the coffeeScript.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected CoffeeScript newCoffeeScript() {
    return new CoffeeScript();
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
