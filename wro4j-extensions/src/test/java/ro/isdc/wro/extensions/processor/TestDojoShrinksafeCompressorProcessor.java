/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test Dojo Shrinksafe compressor processor.
 *
 * @author Alex Objelean
 */
public class TestDojoShrinksafeCompressorProcessor {
  @Test
  public void testFromFolder() throws IOException {
    final ResourcePostProcessor processor = new DojoShrinksafeCompressorProcessor();
    final URL url = getClass().getResource("dojo");

    final File testFolder = new File(ClassLoader.getSystemResource("test").getFile());

    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
  

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(new DojoShrinksafeCompressorProcessor(), ResourceType.JS);
  }
}
