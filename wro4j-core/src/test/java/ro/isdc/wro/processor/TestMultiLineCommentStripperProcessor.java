/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * TestMultiLineCommentStripperPostProcessor.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @created Created on Nov 28, 2008
 */
public class TestMultiLineCommentStripperProcessor extends AbstractWroTest {
  private final MultiLineCommentStripperProcessor processor = new MultiLineCommentStripperProcessor();

  @Test
  public void test1() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/multiline-input.css",
        "classpath:ro/isdc/wro/processor/multiline-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }

  @Test
  public void test2() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/multiline2-input.js",
        "classpath:ro/isdc/wro/processor/multiline2-output.js",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
