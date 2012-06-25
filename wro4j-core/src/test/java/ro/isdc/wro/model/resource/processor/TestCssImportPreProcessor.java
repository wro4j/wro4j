/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.DefaultContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for css import processor.
 *
 * @author Alex Objelean
 */
public class TestCssImportPreProcessor {
  private ResourcePreProcessor processor;

  @Before
  public void setUp() {
    final WroConfiguration config = new WroConfiguration();
    config.setIgnoreFailingProcessor(true);
    DefaultContext.set(DefaultContext.standaloneContext(), config);
    processor = new CssImportPreProcessor();
    WroTestUtils.initProcessor(processor);
  }


  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("cssImport");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
  
  @Test
  public void shouldNotFailWhenInvalidResourceIsFound() throws Exception {
    DefaultContext.get().getConfig().setIgnoreMissingResources(true);
    processInvalidImport();
  }
  
  @Test(expected = IOException.class)
  public void shouldFailWhenInvalidResourceIsFound() throws Exception {
    DefaultContext.get().getConfig().setIgnoreMissingResources(false);
    processInvalidImport();
  }

  
  private void processInvalidImport()
      throws IOException {
    final Resource resource = Resource.create("someResource.css"); 
    final Reader reader = new StringReader("@import('/path/to/invalid.css');");
    processor.process(resource, reader, new StringWriter());
  }
}
