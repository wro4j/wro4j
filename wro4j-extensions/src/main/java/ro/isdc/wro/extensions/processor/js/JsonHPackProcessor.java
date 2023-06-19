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
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.jsonhpack.JsonHPack;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * A processor using json.hpack compression algorithm: @see https://github.com/WebReflection/json.hpack
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class JsonHPackProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(JsonHPackProcessor.class);
  public static final String ALIAS_PACK = "jsonh-pack";
  public static final String ALIAS_UNPACK = "jsonh-unpack";
  /**
   * Engine.
   */
  private ObjectPoolHelper<JsonHPack> enginePool;
  /**
   * If true, the packing will be used, otherwise unpack.
   */
  private boolean pack;

  public JsonHPackProcessor(final boolean pack) {
    this.pack = pack;
    enginePool = new ObjectPoolHelper<JsonHPack>(new ObjectFactory<JsonHPack>() {
      @Override
      public JsonHPack create() {
        return newEngine();
      }
    });
  }

  public static JsonHPackProcessor packProcessor() {
    return new JsonHPackProcessor(true);
  }

  public static JsonHPackProcessor unpackProcessor() {
    return new JsonHPackProcessor(false);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(doProcess(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying hpack processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }


  private String doProcess(final String content) {
    final JsonHPack engine = enginePool.getObject();
    try {
      if (pack) {
        return engine.pack(content);
      }
      return engine.unpack(content);
    } finally {
      enginePool.returnObject(engine);
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
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
