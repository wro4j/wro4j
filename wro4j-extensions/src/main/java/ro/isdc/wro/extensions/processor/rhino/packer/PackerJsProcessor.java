/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.rhino.packer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Compress js using packer utility.
 *
 * @author Alex Objelean
 * @created 31 Jul 2010
 */
@SupportedResourceType(ResourceType.JS)
public class PackerJsProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PackerJsProcessor.class);
  /**
   * Engine.
   */
  private final PackerJs engine;


  public PackerJsProcessor() {
    engine = new PackerJs();
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(engine.pack(content));
    } catch (final Exception e) {
      writer.write(content);
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

}
