/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.impl.AndryCssCompressorProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * @author Alex Objelean
 */
public class TestAndryCssCompressorProcessor extends AbstractWroTest {
  private AndryCssCompressorProcessor processor = new AndryCssCompressorProcessor();
  @Test
  public void test() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/andryCompressor-input.css",
        "classpath:ro/isdc/wro/processor/andryCompressor-output.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(reader, writer);
          }
        });
  }
}
