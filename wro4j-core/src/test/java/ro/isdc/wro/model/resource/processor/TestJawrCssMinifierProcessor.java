/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestJawrCssMinifierProcessor {
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Test
  public void testFromFolder()
      throws IOException {
    final ResourcePostProcessor processor = new JawrCssMinifierProcessor();
    
    final URL url = getClass().getResource("jawrcss");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  
  /**
   * Will transform a cssless resource which was causing an infinite recursion. The test proves that transformation
   * doesn't fail, though the transformed css is not valid anyway.
   */
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
