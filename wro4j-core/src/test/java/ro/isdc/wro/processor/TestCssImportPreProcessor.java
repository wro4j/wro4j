/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
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
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Jul 05, 2009
 */
public class TestCssImportPreProcessor extends AbstractWroTest {
  private final ResourcePreProcessor processor = new CssImportPreProcessor();

  @Test
  public void testValid() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/cssImports/valid-input.css",
        "classpath:ro/isdc/wro/processor/cssImports/valid-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(null, reader, writer);
          }
        });
  }
}
