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
import ro.isdc.wro.extensions.processor.algorithm.cjson.CJson;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * A processor using cjson compression algorithm: {@link http://stevehanov.ca/blog/index.php?id=104}.
 *
 * @author Alex Objelean
 * @since 1.3.8
 * @created 7 Jun 2011
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public abstract class CJsonProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CJsonProcessor.class);
  public static final String ALIAS_PACK = "cjson-pack";
  public static final String ALIAS_UNPACK = "cjson-unpack";
  /**
   * Engine.
   */
  private CJson engine;


  /**
   * Private constructor, prevent instantiation.
   */
  private CJsonProcessor() {}


  public static CJsonProcessor packProcessor() {
    return new CJsonProcessor() {
      @Override
      protected String doProcess(final String content) {
        return getEngine().pack(content);
      }
    };
  }


  public static CJsonProcessor unpackProcessor() {
    return new CJsonProcessor() {
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
  protected void onException(final WroRuntimeException e) {}


  /**
   * A getter used for lazy loading.
   */
  CJson getEngine() {
    if (engine == null) {
      engine = newEngine();
    }
    return engine;
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
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

}
