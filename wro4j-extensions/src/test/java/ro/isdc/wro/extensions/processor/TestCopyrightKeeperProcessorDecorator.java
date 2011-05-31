/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link CopyrightKeeperProcessorDecorator} which decorates processors from extensions library.
 *
 * @author Alex Objelean
 */
public class TestCopyrightKeeperProcessorDecorator {
  @Test
  public void testWithUglifyJs()
      throws Exception {
    final ResourcePreProcessor decoratedProcessor = new UglifyJsProcessor();
    final ResourcePreProcessor processor = CopyrightKeeperProcessorDecorator.decorate(decoratedProcessor);
    final URL url = getClass().getResource("copyright");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
