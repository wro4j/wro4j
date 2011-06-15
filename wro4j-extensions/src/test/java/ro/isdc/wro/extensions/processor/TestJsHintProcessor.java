/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test {@link JsHintProcessor}.
 *
 * @author Alex Objelean
 * @created Created on Feb 27, 2011
 */
public class TestJsHintProcessor {
  private ResourceProcessor processor;


  @Before
  public void setUp() {
    processor = new JsHintProcessor();
  }


  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("jsHint");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
