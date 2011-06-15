/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test sass css processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestSassCssProcessor {
  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("sasscss");
    final ResourceProcessor processor = new SassCssProcessor();

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }
}
