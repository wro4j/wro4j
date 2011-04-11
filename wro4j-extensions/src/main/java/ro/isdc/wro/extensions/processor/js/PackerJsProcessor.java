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
import ro.isdc.wro.extensions.processor.algorithm.packer.PackerJs;
import ro.isdc.wro.model.group.processor.Minimize;
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
@Minimize
@SupportedResourceType(ResourceType.JS)
public class PackerJsProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(PackerJsProcessor.class);
  /**
   * Engine.
   */
  private PackerJs engine;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(getEngine().pack(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the resource, no processing applied...", e);
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
   * @return PackerJs engine.
   */
  private PackerJs getEngine() {
    if (engine == null) {
      engine = new PackerJs();
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
