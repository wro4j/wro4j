/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.test.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * ResourceProcessor.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @created Created on Mar 10, 2009
 */
public interface ResourceProcessor {
  /**
   * Process resource content.
   *
   * @param reader
   *          from where to read resource content.
   * @param writer
   *          where the processed result is written.
   * @throws IOException
   *           if IO errors occurs during resource processing
   */
  void process(Reader reader, Writer writer) throws IOException;
}
