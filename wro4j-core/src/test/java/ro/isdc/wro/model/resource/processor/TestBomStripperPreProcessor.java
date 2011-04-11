/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test class for {@link BomStripperPreProcessor}
 *
 * @author Alex Objelean
 */
public class TestBomStripperPreProcessor {
  private final ResourcePreProcessor processor = new BomStripperPreProcessor();


  @Test
  public void testFromFolder()
    throws Exception {
    final URL url = getClass().getResource("bom");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
