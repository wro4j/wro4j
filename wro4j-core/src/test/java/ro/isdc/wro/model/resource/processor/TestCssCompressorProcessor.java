/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestCssCompressorProcessor {
  private final ResourcePreProcessor processor = new CssCompressorProcessor();


  @Test
  public void test()
    throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:ro/isdc/wro/processor/cssCompressor-input.css",
      "classpath:ro/isdc/wro/processor/cssCompressor-output.css", processor);
  }
}
