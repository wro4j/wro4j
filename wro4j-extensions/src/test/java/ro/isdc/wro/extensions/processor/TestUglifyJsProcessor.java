/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestUglifyJsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestUglifyJsProcessor {
  private File testFolder;
  @Before
  public void setUp() {
    testFolder = new File(ClassLoader.getSystemResource("test").getFile());
  }
  @Test
  public void testFromFolder() throws IOException {
    final ResourceProcessor processor = new UglifyJsProcessor();
    final URL url = getClass().getResource("uglify");

    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
