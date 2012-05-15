/*
 * Copyright (c) 2012. All rights reserved.
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
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * A processor using the ruby sass engine:
 * 
 * @author Simon van der Sluis
 * @since 1.4.6
 * @created 2/03/12
 */
@SupportedResourceType(ResourceType.CSS)
public class RubySassCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(RubySassCssProcessor.class);
  public static final String ALIAS = "rubySassCss";
  
  /**
   * Engine.
   */
  private RubySassEngine engine;
  
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
  protected RubySassEngine getEngine() {
    if (engine == null) {
      engine = new RubySassEngine();
    }
    return engine;
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
  
}
