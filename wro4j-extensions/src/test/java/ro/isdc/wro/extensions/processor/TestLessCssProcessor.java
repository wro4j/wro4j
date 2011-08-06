/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test less css processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 21, 2010
 */
public class TestLessCssProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(TestLessCssProcessor.class);

  @Test
  public void testFromFolder()
      throws Exception {
    final ResourcePostProcessor processor = new LessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }

//  @Test(expected=WroRuntimeException.class)
  public void testInvalidLessCss()
      throws Exception {
    final ResourcePostProcessor processor = new LessCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        LOG.debug("Exception message is: " + e.getMessage());
        throw e;
      };
    };
    final Reader reader = new InputStreamReader(getClass().getResourceAsStream("lesscss/giveErrors.less"));
    processor.process(reader, new StringWriter());
  }
}
