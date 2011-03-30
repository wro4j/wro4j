/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.util.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestCssCompressorProcessor {
  private final CssCompressorProcessor processor = new CssCompressorProcessor();

  @Test
  public void test()
      throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/cssCompressor-input.css",
        "classpath:ro/isdc/wro/processor/cssCompressor-output.css", new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
