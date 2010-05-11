/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * Test for css variables preprocessor.
 *
 * @author Alex Objelean
 * @created Created on Jul 05, 2009
 */
public class TestCssVariablesProcessor extends AbstractWroTest {
  private final ResourcePreProcessor processor = new CssVariablesProcessor();

  @Test
  public void testValid() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/cssvariables/valid-input.css",
        "classpath:ro/isdc/wro/processor/cssvariables/valid-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(null, reader, writer);
          }
        });
  }
}
