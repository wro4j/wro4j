/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestJsMinProcessor {
  @Test
  public void testAsPostProcessor()
      throws IOException {
    final ResourcePreProcessor processor = new JSMinProcessor();
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/jsmin-input.js",
        "classpath:ro/isdc/wro/processor/jsmin-output.js", processor);
  }

  @Test
  public void testAsPreProcessor()
      throws IOException {
    final ResourcePreProcessor processor = new JSMinProcessor();
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/jsmin-input.js",
        "classpath:ro/isdc/wro/processor/jsmin-output.js", processor);
  }
}
