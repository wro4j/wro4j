/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Ignore;
import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;


/**
 * Test class for {@link BomStripperPreProcessor}
 *
 * @author Alex Objelean
 */
public class TestBomStripperPreProcessor extends AbstractWroTest {
  private final ResourcePreProcessor processor = new BomStripperPreProcessor();

  @Test
  public void testWithBom() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/bom/bom-input.js",
        "classpath:ro/isdc/wro/processor/bom/bom-output.js",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(null, reader, writer);
          }
        });
  }

  /**
   * Check if the stream which doesn't contain BOM characters remains unchanged.
   * @throws IOException
   */
  @Test
  public void testWithoutBom() throws IOException {
    compareProcessedResourceContents(
        "classpath:ro/isdc/wro/processor/bom/nobom.js",
        "classpath:ro/isdc/wro/processor/bom/nobom.js",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(null, reader, writer);
          }
        });
  }

  /**
   * This test is ignored because it fails when run outside of IDE (encoding issues)
   */
  @Ignore
  @Test
  public void testWithChineseCharacters() throws IOException {
    compareProcessedResourceContents(
        "http://wro4j.googlecode.com/svn/wiki/static/encoding/chinese.js",
        "classpath:ro/isdc/wro/processor/bom/chineseEncoding-output.js",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(null, reader, writer);
          }
        });
  }

}
