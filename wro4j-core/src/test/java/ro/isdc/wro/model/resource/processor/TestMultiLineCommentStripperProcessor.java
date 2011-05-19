/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestMultiLineCommentStripperProcessor {
  private final ResourcePreProcessor processor = new MultiLineCommentStripperProcessor();


  @Test
  public void test1()
    throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/multiline-input.css",
      "classpath:ro/isdc/wro/processor/multiline-output.css", processor);
  }


  @Test
  public void test2()
    throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/multiline2-input.js",
      "classpath:ro/isdc/wro/processor/multiline2-output.js", processor);
  }
}
