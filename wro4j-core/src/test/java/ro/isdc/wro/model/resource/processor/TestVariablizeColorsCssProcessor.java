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
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestVariablizeColorsCssProcessor.
 * 
 * @author Alex Objelean
 * @created Created on Aug 15, 2010
 */
public class TestVariablizeColorsCssProcessor {
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
    processor = new VariablizeColorsCssProcessor();
    Context.set(Context.standaloneContext());
    WroTestUtils.initProcessor(processor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("variablizeColors");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  
  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
