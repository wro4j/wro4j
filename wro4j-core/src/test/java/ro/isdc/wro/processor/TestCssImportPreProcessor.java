/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.processor.impl.CssImportPreProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * Test for css variables preprocessor.
 *
 * @author Alex Objelean
 * @created Created on Jul 05, 2009
 */
public class TestCssImportPreProcessor extends AbstractWroTest {
  private final ResourcePreProcessor processor = new CssImportPreProcessor();

  @Test
  public void testValid() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/cssImports/test1-input.css",
        "classpath:ro/isdc/wro/processor/cssImports/test1-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(null, reader, writer);
          }
        });
  }
}
