/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.algorithm.ResourceContentStripper;

/**
 * Removes comments and whitespaces from resource content. This processor can be
 * used as both: preProcessor & postProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 13, 2008
 */
public final class ResourceContentStripperProcessor implements
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
