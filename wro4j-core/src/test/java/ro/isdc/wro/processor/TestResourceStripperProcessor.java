/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.ResourceContentStripperProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * Test class for {@link ResourceContentStripperProcessor}
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestResourceStripperProcessor extends AbstractWroTest {
  private final ResourcePostProcessor processor = new ResourceContentStripperProcessor();

  @Test
  public void test() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/resourceContentStripper-input.css",
        "classpath:ro/isdc/wro/processor/resourceContentStripper-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
