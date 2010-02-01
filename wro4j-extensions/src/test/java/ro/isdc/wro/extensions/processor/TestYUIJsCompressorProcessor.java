/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * TestMultiLineCommentStripperPostProcessor.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 28, 2008
 */
public class TestYUIJsCompressorProcessor extends AbstractWroTest {
  private final ResourcePostProcessor processor = new YUIJsCompressorProcessor();

  @Test
  public void test() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/extensions/processor/yuijscompressor-input.js",
        "classpath:ro/isdc/wro/extensions/processor/yuijscompressor-output.js",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
