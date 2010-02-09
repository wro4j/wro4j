/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.algorithm.JSMin;

/**
 * JSMinProcessor.
 * <p>
 * Use JSMin utility for js compression.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
@SupportedResourceType(type=ResourceType.JS)
public class JSMinProcessor implements ResourcePreProcessor,
    ResourcePostProcessor {
  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    try {
      final InputStream is = new ByteArrayInputStream(IOUtils
          .toByteArray(reader));
      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      final JSMin jsmin = new JSMin(is, os);
      jsmin.jsmin();
      IOUtils.write(os.toByteArray(), writer);
      is.close();
      os.close();
    } catch (final IOException e) {
      throw e;
		} catch (final Exception e) {
			throw new WroRuntimeException("Exception wile processing js using JSMin", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException {
    // resource Uri doesn't matter.
    process(reader, writer);
  }
}
