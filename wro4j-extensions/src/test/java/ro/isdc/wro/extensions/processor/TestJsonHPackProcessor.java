/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsonHPackProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test json hpack processor.
 *
 * @author Alex Objelean
 * @created Created on June 07, 2011
 */
public class TestJsonHPackProcessor {

  @Test
  public void testPackFromFolder()
      throws Exception {
    final ResourceProcessor processor = JsonHPackProcessor.packProcessor();
    final URL url = getClass().getResource("jsonhpack");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "pack");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

  @Test
  public void testUnpackFromFolder()
      throws Exception {
    final ResourceProcessor processor = JsonHPackProcessor.unpackProcessor();
    final URL url = getClass().getResource("jsonhpack");

    final File testFolder = new File(url.getFile(), "pack");
    final File expectedFolder = new File(url.getFile(), "unpack");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

}
