/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author Alex Objelean
 */
public class TestCommentStripperProcessor {
  private final ResourceProcessor processor = new CommentStripperProcessor();

  @Test
  public void test()
      throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/comment-input.js",
      "classpath:ro/isdc/wro/processor/comment-output.js", processor);
  }
}
