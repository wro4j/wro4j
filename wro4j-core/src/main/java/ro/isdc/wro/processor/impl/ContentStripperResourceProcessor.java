/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.processor.algorithm.ResourceContentStripper;
import ro.isdc.wro.resource.Resource;

/**
 * TODO: deprecate & use other proven solutions.
 * Removes comments and whitespaces from resource content. This processor can be
 * used as both: preProcessor & postProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 13, 2008
 */
@Deprecated
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
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException {
    process(reader, writer);
  }
}
