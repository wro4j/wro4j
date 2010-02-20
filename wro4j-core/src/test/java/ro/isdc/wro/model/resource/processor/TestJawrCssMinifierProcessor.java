/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.JawrCssMinifierProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * @author Alex Objelean
 */
public class TestJawrCssMinifierProcessor extends AbstractWroTest {
  private ResourcePostProcessor processor = new JawrCssMinifierProcessor();
  @Test
  public void test() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/jawrCssProcessor-input.css",
        "classpath:ro/isdc/wro/processor/jawrCssProcessor-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
