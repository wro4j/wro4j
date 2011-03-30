/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * TestBeautifyJsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestBeautifyJsProcessor {
  @Test
  public void testFromFolder() throws IOException {
    final ResourcePostProcessor processor = new BeautifyJsProcessor();
    final URL url = getClass().getResource("beautify");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      WroUtil.newResourceProcessor(processor));
  }
}
