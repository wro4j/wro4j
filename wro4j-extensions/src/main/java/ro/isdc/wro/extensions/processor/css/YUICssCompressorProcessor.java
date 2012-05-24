/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.PlaceholderProcessor;
import ro.isdc.wro.util.WroUtil;

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
  implements ResourcePostProcessor, ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(YUICssCompressorProcessor.class);
  public static final String ALIAS = "yuiCssMin";
  /**
   * An option of CssCompressor.
   */
  private static final int linebreakpos = -1;

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final CssCompressor compressor = new CssCompressor(reader);
      compressor.compress(writer, linebreakpos);
    } catch(IOException e) {
      LOG.error("Exception occured while processing resource: " + resource + " using processor: " + ALIAS);
      onException(new WroRuntimeException("Exception during YuiCss processing of resource: " + resource, e));
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }
}
