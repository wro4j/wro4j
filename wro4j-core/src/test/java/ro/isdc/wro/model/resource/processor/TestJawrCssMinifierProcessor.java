/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestJawrCssMinifierProcessor {
  @Test
  public void testFromFolder() throws IOException {
    final ResourcePostProcessor processor = new JawrCssMinifierProcessor();

    final URL url = getClass().getResource("jawrcss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css",
      processor);
  }
  
  @Test
  public void shouldHandleInvalidResources()
      throws IOException {
    final ResourcePostProcessor processor = new JawrCssMinifierProcessor();
    
    final URL url = getClass().getResource("jawrcss");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedInvalid");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new JawrCssMinifierProcessor(), ResourceType.CSS);
  }
}
