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
import ro.isdc.wro.extensions.processor.support.cjson.CJson;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * A processor using <a href="http://stevehanov.ca/blog/index.php?id=104">cjson compression algorithm</a>.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class CJsonProcessor
  implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(CJsonProcessor.class);
  public static final String ALIAS_PACK = "cjson-pack";
  public static final String ALIAS_UNPACK = "cjson-unpack";
  /**
   * Engine.
   */
  private ObjectPoolHelper<CJson> enginePool;
  /**
   * If true, the packing will be used, otherwise unpack.
   */
  private boolean pack;

  /**
   * Private constructor, prevent instantiation.
   */
  public CJsonProcessor(final boolean pack) {
    enginePool = new ObjectPoolHelper<CJson>(new ObjectFactory<CJson>() {
      @Override
      public CJson create() {
        return newEngine();
      }
    });
    this.pack = pack;
  }


  public static CJsonProcessor packProcessor() {
    return new CJsonProcessor(true);
  }


  public static CJsonProcessor unpackProcessor() {
    return new CJsonProcessor(false);
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
      LOG.warn("Exception while  applying lessCss processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  private String doProcess(final String content) {
    final CJson engine = enginePool.getObject();
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
   * @return the {@link CJson} engine implementation. Override it to provide a different version of the json.hpack.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected CJson newEngine() {
    return new CJson();
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
