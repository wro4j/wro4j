/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.processor.algorithm.JSMin;
import ro.isdc.wro.resource.Resource;

/**
 * JSMinProcessor.
 * <p>
 * Use JSMin utility for js compression.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 28, 2008
 */
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
      throw new WroRuntimeException("Exception wile processing js using JSMin",
          e);
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
