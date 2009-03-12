/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * ResourcePreProcessor. A processor which will be applied to the resource
 * before merging.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 19, 2008
 */
public interface ResourcePreProcessor {
  /**
   * Process a content supplied by a reader and perform some sort of processing.
   * 
   * @param resourceUri
   *          Uri of the original resource as it found in the model.
   * @param reader
   *          {@link Reader} used to read original resource content.
   * @param writer
   *          {@link Writer} where used to write processed results.
   * @throws IOException
   *           when IO exception occurs.
   */
  public void process(final String resourceUri, final Reader reader,
      final Writer writer) throws IOException;
}
