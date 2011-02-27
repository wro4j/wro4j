/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsHintPreProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test {@link JsHintPreProcessor}.
 *
 * @author Alex Objelean
 * @created Created on Feb 27, 2011
 */
public class TestJsHintProcessor {
  private ResourcePreProcessor processor;

  @Before
  public void setUp() {
    processor = new JsHintPreProcessor();
  }

  @Test
  public void testFromFolder() throws IOException {
    final URL url = getClass().getResource("jsHint");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      WroUtil.newResourceProcessor(processor));
  }
}
