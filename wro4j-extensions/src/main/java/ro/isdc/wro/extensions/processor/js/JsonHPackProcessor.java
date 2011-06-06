/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.algorithm.jsonhpack.JsonHPack;
import ro.isdc.wro.extensions.processor.algorithm.less.LessCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * A processor using lessCss engine: @see https://github.com/WebReflection/json.hpack
 *
 * @author Alex Objelean
 * @since 1.2.6
 * @created 21 Apr 2010
 */
@SupportedResourceType(ResourceType.JS)
public class JsonHPackProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(JsonHPackProcessor.class);
  /**
   * Engine.
   */
  private JsonHPack engine;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(getEngine().less(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      LOG.warn("Exception while  applying lessCss processor on the resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
  }


  /**
   * A getter used for lazy loading.
   */
  private JsonHPack getEngine() {
    if (engine == null) {
      engine = newEngine();
    }
    return engine;
  }


  /**
   * @return the {@link LessCss} engine implementation. Override it to provide a different version of the less.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected JsonHPack newEngine() {
    return new JsonHPack();
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

}
