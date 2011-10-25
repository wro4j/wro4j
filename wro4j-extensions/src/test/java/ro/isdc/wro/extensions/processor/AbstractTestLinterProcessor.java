/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test {@link JsHintProcessor}.
 *
 * @author Alex Objelean
 * @created Created on Feb 27, 2011
 */
public abstract class AbstractTestLinterProcessor {
  private final ResourcePreProcessor processor = newLinterProcessor();

  /**
   * @return linter processor to test.
   */
  protected abstract ResourcePreProcessor newLinterProcessor();

  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("jsHint");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }
}
