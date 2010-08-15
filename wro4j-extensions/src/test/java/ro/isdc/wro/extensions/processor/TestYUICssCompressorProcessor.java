/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestYUICssCompressorProcessor extends AbstractWroTest {
  private final ResourcePostProcessor processor = new YUICssCompressorProcessor();

  @Test
  public void test() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/extensions/processor/yuicsscompressor-input.css",
        "classpath:ro/isdc/wro/extensions/processor/yuicsscompressor-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }


  @Test
  public void testAbsoluteBackgroundUrl()
    throws IOException {
    //Output should be the same as input.
    final String resourcePath = "classpath:ro/isdc/wro/extensions/processor/absoluteBackgroundUrl.css";
    compareProcessedResourceContents(resourcePath, resourcePath, new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
            throws IOException {
          processor.process(reader, writer);
        }
      });
  }
}
