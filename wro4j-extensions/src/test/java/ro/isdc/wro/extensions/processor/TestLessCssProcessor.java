/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
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
    final ResourceProcessor processor = new LessCssProcessor();
    final URL url = getClass().getResource("lesscss");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }


  @Test
  public void executeMultipleTimesDoesntThrowOutOfMemoryException() {
    final LessCss lessCss = new LessCss();
    for (int i = 0; i < 100; i++) {
      lessCss.less("#id {.class {color: red;}}");
    }
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final LessCssProcessor lessCss = new LessCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        throw e;
      }
    };
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          lessCss.process(new StringReader("#id {.class {color: red;}}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }


  @Test(expected=WroRuntimeException.class)
  public void testInvalidLessCss()
      throws Exception {
    final ResourceProcessor processor = new LessCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        LOG.debug("[FAIL] Exception message is: {}", e.getMessage());
        throw e;
      };
    };
    final Reader reader = new InputStreamReader(getClass().getResourceAsStream("lesscss/giveErrors.less"));
    processor.process(null, reader, new StringWriter());
  }
}
