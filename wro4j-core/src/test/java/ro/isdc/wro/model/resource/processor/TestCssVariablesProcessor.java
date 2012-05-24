/*
 * Copyright (c) 2008.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for css variables preprocessor.
 *
 * @author Alex Objelean
 * @created Created on Jul 05, 2009
 */
public class TestCssVariablesProcessor {
  private final ResourcePreProcessor processor = new CssVariablesProcessor();

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("cssvariables");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
