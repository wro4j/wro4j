/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.resource.Resource;


/**
 * ResourcePreProcessor. A processor which will be applied to the resource before merging. TODO: maybe it would be a
 * good idea to have List<ResourceType> support() method used to identify the type of resources this should apply on?
 *
 * @author Alex Objelean
 * @created Created on Nov 19, 2008
 */
public interface ResourcePreProcessor {
  //TODO add a closure responsible for applying preprocessor on new resources instead of modifying the model.
  /**
   * Process a content supplied by a reader and perform some sort of processing.
   *
   * @param resource
   *          the original resource as it found in the model.
   * @param reader
   *          {@link Reader} used to read original resource content.
   * @param writer
   *          {@link Writer} where used to write processed results.
   * @throws IOException
   *           when IO exception occurs.
   */
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException;
}
