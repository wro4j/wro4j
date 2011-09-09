/*
 * Copyright (C) 2011. All rights reserved.
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
import ro.isdc.wro.extensions.processor.support.jsonhpack.JsonHPack;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * A processor using json.hpack compression algorithm: @see https://github.com/WebReflection/json.hpack
 *
 * @author Alex Objelean
 * @since 1.3.8
 * @created 7 Jun 2011
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public abstract class JsonHPackProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(JsonHPackProcessor.class);
  public static final String ALIAS_PACK = "jsonh-pack";
  public static final String ALIAS_UNPACK = "jsonh-unpack";
  /**
   * Engine.
   */
  private JsonHPack engine;

  /**
   * Private constructor, prevent instantiation.
   */
  private JsonHPackProcessor() {
  }

  public static JsonHPackProcessor packProcessor() {
    return new JsonHPackProcessor() {
      @Override
      protected String doProcess(final String content) {
        return getEngine().pack(content);
      }
    };
  }

  public static JsonHPackProcessor unpackProcessor() {
    return new JsonHPackProcessor() {
      @Override
      protected String doProcess(final String content) {
        return getEngine().unpack(content);
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(doProcess(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while  applying lessCss processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }


  protected abstract String doProcess(final String content);

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
  }

  /**
   * A getter used for lazy loading.
   */
  JsonHPack getEngine() {
    if (engine == null) {
      engine = newEngine();
    }
    return engine;
  }

  /**
   * @return the {@link JsonHPack} engine implementation. Override it to provide a different version of the
   *         json.hpack.js library. Useful for upgrading the processor outside the wro4j release.
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
