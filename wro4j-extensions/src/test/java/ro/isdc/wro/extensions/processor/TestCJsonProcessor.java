/*
 * Copyright (c) 2011. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.CJsonProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test cjson processor.
 *
 * @author Alex Objelean
 * @created Created on June 07, 2011
 */
public class TestCJsonProcessor {

  @Test
  public void testPackFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = CJsonProcessor.packProcessor();
    final URL url = getClass().getResource("cjson");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "pack");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

  @Test
  public void testUnpackFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = CJsonProcessor.unpackProcessor();
    final URL url = getClass().getResource("cjson");

    final File testFolder = new File(url.getFile(), "pack");
    final File expectedFolder = new File(url.getFile(), "unpack");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }

}
