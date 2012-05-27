/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestConformColorsCssProcessor.
 *
 * @author Alex Objelean
 * @created Created on Aug 15, 2010
 */
public class TestConformColorsCssProcessor {
  private ResourceProcessor processor;

  @Before
  public void setUp() {
    processor = new ConformColorsCssProcessor();
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("conformColors");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
  

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
