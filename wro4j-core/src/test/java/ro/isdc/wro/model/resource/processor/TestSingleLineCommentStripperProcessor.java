/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.util.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestSingleLineCommentStripperProcessor {
  private final ResourcePostProcessor processor = new SingleLineCommentStripperProcessor();

  @Test
  public void test()
      throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/singleline-input.js",
        "classpath:ro/isdc/wro/processor/singleline-output.js", new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }

  // @Test
  // public void testAbsoluteBackgroundUrl()
  // throws IOException {
  // //Output should be the same as input.
  // final String resourcePath = "classpath:ro/isdc/wro/processor/absolutBackgroundUrl.css";
  // compareProcessedResourceContents(resourcePath, resourcePath, new ResourceProcessor() {
  // public void process(final Reader reader, final Writer writer)
  // throws IOException {
  // processor.process(reader, writer);
  // }
  // });
  // }
}
