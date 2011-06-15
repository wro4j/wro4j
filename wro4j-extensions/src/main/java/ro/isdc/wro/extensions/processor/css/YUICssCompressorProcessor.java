/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;

import com.yahoo.platform.yui.compressor.CssCompressor;


/**
 * YUICssCompressorProcessor. Use YUI css compression utility for processing a css resource.
 *
 * @author Alex Objelean
 * @created Created on Dec 4, 2008
 */
@Minimize
@SupportedResourceType(ResourceType.CSS)
public class YUICssCompressorProcessor
  implements ResourceProcessor {
  /**
   * An option of CssCompressor.
   */
  private static final int linebreakpos = -1;

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final CssCompressor compressor = new CssCompressor(reader);
      compressor.compress(writer, linebreakpos);
    } finally {
      reader.close();
      writer.close();
    }
  }
}
