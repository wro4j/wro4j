/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.DefaultContext;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestJsMinProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestJsMinProcessor {
  private ResourcePreProcessor processor;
  @Before
  public void setUp() {
    processor = new JSMinProcessor();
    DefaultContext.set(DefaultContext.standaloneContext());
    WroTestUtils.createInjector().inject(processor);
  }

  @After
  public void tearDown() {
    DefaultContext.unset();
  }

  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("jsmin");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
  

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.JS);
  }
}
