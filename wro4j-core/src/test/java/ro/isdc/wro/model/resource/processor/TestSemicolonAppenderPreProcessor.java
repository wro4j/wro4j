/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link SemicolonAppenderPreProcessor} class.
 *
 * @author Alex Objelean
 * @created Created on March 21, 2010
 */
public class TestSemicolonAppenderPreProcessor {
  private final ResourcePreProcessor processor = new SemicolonAppenderPreProcessor();

  @Test
  public void test()
      throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/jsSemicolonAppender-input.js",
        "classpath:ro/isdc/wro/processor/jsSemicolonAppender-output.js", processor);
  }
}
