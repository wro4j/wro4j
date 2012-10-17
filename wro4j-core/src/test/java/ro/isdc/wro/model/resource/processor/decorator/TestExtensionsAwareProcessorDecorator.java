/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.decorator;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestExtensionsAwareProcessorDecorator {
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @Test
  public void shouldApplyProcessorOnlyOnResourcesWithExtensionJs()
      throws Exception {
    final ResourceProcessor decoratedProcessor = new JSMinProcessor();
    final ResourceProcessor processor = ExtensionsAwareProcessorDecorator.decorate(decoratedProcessor).addExtension(
        "js");
    WroTestUtils.createInjector().inject(processor);
    // we use test resource relative to TestProcessorsUtils class
    final URL url = ResourceProcessor.class.getResource("extensionAware");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFolders(testFolder, expectedFolder, processor);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullExtension() {
    final ResourceProcessor decoratedProcessor = new JSMinProcessor();
    ExtensionsAwareProcessorDecorator.decorate(decoratedProcessor).addExtension(null);
  }

  @Test
  public void testMinimizeAwareDecorator1() {
    final ResourceProcessor decoratedProcessor = new JSMinProcessor();
    final ResourceProcessor processor = ExtensionsAwareProcessorDecorator.decorate(decoratedProcessor);
    Assert.assertTrue(new ProcessorDecorator(processor).isMinimize());
  }

  @Test
  public void testMinimizeAwareDecorator2() {
    final ResourceProcessor decoratedProcessor = new CssUrlRewritingProcessor();
    final ResourceProcessor processor = ExtensionsAwareProcessorDecorator.decorate(decoratedProcessor);
    Assert.assertFalse(new ProcessorDecorator(processor).isMinimize());
  }
}
