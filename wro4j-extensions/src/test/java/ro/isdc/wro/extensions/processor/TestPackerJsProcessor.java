/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test packer js processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestPackerJsProcessor {
  private ResourceProcessor processor;
  private File testFolder;

  @Before
  public void setUp() {
    testFolder = new File(ClassLoader.getSystemResource("test").getFile());
    processor = new PackerJsProcessor();
  }

  @Test
  public void testFromFolder() throws IOException {
    final URL url = getClass().getResource("packer");

    //final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
