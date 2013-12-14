/*
 * Copyright (c) 2008. All rights reserved.
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
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link SemicolonAppenderPreProcessor} class.
 * 
 * @author Alex Objelean
 * @created Created on March 21, 2010
 */
public class TestSemicolonAppenderPreProcessor {
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
    final ResourcePreProcessor processor = new SemicolonAppenderPreProcessor();
    
    final URL url = getClass().getResource("semicolonAppender");
    
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
  
  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new SemicolonAppenderPreProcessor(), ResourceType.JS);
  }
}
