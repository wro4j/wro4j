/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A processor which is applied after the merge. Examples are: JSCompressor.
 *
 * @author Alex Objelean
 */
public interface ResourcePostProcessor {
  /**
   * Perform actual resource processing. The content to be processed is read
   * from source Reader and is written to destination Writer. It is the client
   * responsibility to close both: Reader and writer.
   *
   * @param reader
   *          source stream.
   * @param writer
   *          destination stream.
   * @throws IOException when the resource cannot be processed.
   */
  void process(final Reader reader, final Writer writer)
      throws IOException;
}
