/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link CopyrightKeeperProcessorDecorator} which decorates processors from extensions library.
 *
 * @author Alex Objelean
 */
public class TestCopyrightKeeperProcessorDecorator {
  private ResourcePreProcessor processor;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    final ResourcePreProcessor decoratedProcessor = new JSMinProcessor();
    processor = CopyrightKeeperProcessorDecorator.decorate(decoratedProcessor);
    Context.set(Context.standaloneContext());
    WroTestUtils.createInjector().inject(decoratedProcessor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void decorateJsMinProcessor()
      throws Exception {
    final URL url = getClass().getResource("copyright");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
