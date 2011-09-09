/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.sass.SassCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * A processor using sass engine:
 *
 * @author Alex Objelean
 * @created 27 Oct 2010
 */
@SupportedResourceType(ResourceType.CSS)
public class SassCssProcessor
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SassCssProcessor.class);
  public static final String ALIAS = "sassCss";
  /**
   * Engine.
   */
  private SassCss engine;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(getEngine().process(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
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
  private SassCss getEngine() {
    if (engine == null) {
      engine = new SassCss();
    }
    return engine;
  }
}
