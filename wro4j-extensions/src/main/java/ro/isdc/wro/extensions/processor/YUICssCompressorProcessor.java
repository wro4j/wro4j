/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;

import com.yahoo.platform.yui.compressor.CssCompressor;


/**
 * YUICssCompressorProcessor. Use YUI css compression utility for processing a css resource.
 *
 * @author Alexandru.Objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Dec 4, 2008
 */
public class YUICssCompressorProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * An option of CssCompressor.
   */
  private static final int linebreakpos = -1;


  /**
   * {@inheritDoc}
   */
  public void process(final String resourceUri, final Reader reader, final Writer writer)
    throws IOException {
    // resourceUri doesn't matter
    this.process(reader, writer);
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    final CssCompressor compressor = new CssCompressor(reader);
    compressor.compress(writer, linebreakpos);
  }
}
