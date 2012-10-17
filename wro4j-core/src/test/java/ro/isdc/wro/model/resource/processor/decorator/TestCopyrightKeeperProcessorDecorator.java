/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.decorator;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * @author Alex Objelean
 */
public class TestCopyrightKeeperProcessorDecorator {
  @Test
  public void testCopyrightStripperProcessor()
      throws Exception {
    final ResourceProcessor decoratedProcessor = new CssMinProcessor();
    final ResourceProcessor processor = CopyrightKeeperProcessorDecorator.decorate(decoratedProcessor);
    final URL url = ResourceProcessor.class.getResource("copyright");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void testCopyrightAwareProcessor()
      throws Exception {
    //This procesor won't remove copyright headers.
    final ResourceProcessor decoratedProcessor = new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
        throws IOException {
        IOUtils.copy(reader, writer);
      }
    };
    final ResourceProcessor processor = CopyrightKeeperProcessorDecorator.decorate(decoratedProcessor);
    final URL url = ResourceProcessor.class.getResource("copyright");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedCopyrightAware");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void testMinimizeAwareDecorator1() {
    final ResourceProcessor decoratedProcessor = new JSMinProcessor();
    final ResourceProcessor processor = CopyrightKeeperProcessorDecorator.decorate(decoratedProcessor);
    Assert.assertTrue(new ProcessorDecorator(processor).isMinimize());
  }

  @Test
  public void testMinimizeAwareDecorator2() {
    final ResourceProcessor decoratedProcessor = new CssUrlRewritingProcessor();
    final ResourceProcessor processor = CopyrightKeeperProcessorDecorator.decorate(decoratedProcessor);
    Assert.assertFalse(new ProcessorDecorator(processor).isMinimize());
  }
}
