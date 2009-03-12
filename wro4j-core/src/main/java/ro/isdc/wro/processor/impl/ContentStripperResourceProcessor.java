/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ResourceContentStripper;

/**
 * Removes comments and whitespaces from resource content. This processor can be
 * used as both: preProcessor & postProcessor.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 13, 2008
 */
public final class ContentStripperResourceProcessor implements
    ResourcePreProcessor, ResourcePostProcessor {
  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    writer.write(ResourceContentStripper.stripCommentsAndWhitespace(IOUtils
        .toString(reader)));
  }

  /**
   * {@inheritDoc}
   */
  public void process(final String resourceUri, final Reader reader,
      final Writer writer) throws IOException {
    process(reader, writer);
  }
}
