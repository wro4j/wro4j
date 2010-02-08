/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.resource.Resource;


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
   * Process a content supplied by a reader and perform some sort of processing. It is important to know that you should
   * use reader for processing instead of trying to access the resource original content using {@link Resource}, because
   * this way you can ignore the other preProcessors from the chain.
   *
   * @param resource the original resource as it found in the model.
   * @param reader {@link Reader} used to read processed resource content.
   * @param writer {@link Writer} where used to write processed results.
   * @throws IOException when IO exception occurs.
   */
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException;
}
