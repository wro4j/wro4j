/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.FallbackCssDataUriProcessor;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link CssDataUriPreProcessor} class.
 * 
 * @author Alex Objelean
 * @created Created on Mat 09, 2010
 */
public class TestFallbackCssDataUriProcessor
    extends TestCssDataUriPreProcessor {
  private ResourcePreProcessor processor;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Override
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    processor = new FallbackCssDataUriProcessor() {
      @Override
      protected DataUriGenerator getDataUriGenerator() {
        return createMockDataUriGenerator();
      }
    };
    // find a way to use a custom uriLocator
    initProcessor(processor);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Override
  @Test
  public void shouldTransformResourcesFromFolder()
      throws Exception {
    final URL url = getClass().getResource("dataUri");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedFallback");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  
  @Override
  @Test
  public void shouldTransformLargeResources()
      throws Exception {
    processor = new CssDataUriPreProcessor();
    initProcessor(processor);
    
    final URL url = getClass().getResource("dataUri");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedFallbackLarge");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  
  @Override
  @Test
  public void shouldSupportOnlyCssResources() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
