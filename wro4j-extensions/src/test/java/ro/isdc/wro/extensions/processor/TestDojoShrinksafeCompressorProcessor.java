/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test Dojo Shrinksafe compressor processor.
 *
 * @author Alex Objelean
 * @created Created on Nov 6, 2010
 */
public class TestDojoShrinksafeCompressorProcessor {
  @Test
  public void testFromFolder() throws IOException {
    final ResourceProcessor processor = new DojoShrinksafeCompressorProcessor();
    final URL url = getClass().getResource("dojo");

    final File testFolder = new File(ClassLoader.getSystemResource("test").getFile());

    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
