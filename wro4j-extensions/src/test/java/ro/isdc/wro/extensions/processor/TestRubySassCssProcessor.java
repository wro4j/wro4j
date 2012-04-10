/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import junit.framework.Assert;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.Transformers;
import ro.isdc.wro.util.WroTestUtils;

import java.io.*;
import java.net.URL;
import java.util.concurrent.Callable;


/**
 * Test ruby sass css processor.
 *
 * @author Simon van der Sluis
 * @created Created on Apr 21, 2010
 */
public class TestRubySassCssProcessor
{

  private static final Logger LOG = LoggerFactory.getLogger(TestRubySassCssProcessor.class);

  /** Location (base) of ruby sass css test resources. */
  private final URL url = getClass().getResource("rubysasscss");

  /** A RubySassEngine to test */
  private RubySassCssProcessor rubySassCss;

  @Before
  public void initEngine() {
    rubySassCss = new RubySassCssProcessor() {
      @Override
      protected void onException(final WroRuntimeException e) {
        LOG.debug("[FAIL] Exception message is: {}", e.getMessage());
        throw e;
      }
    };
  }

  @Test
  public void testFromFolder()
    throws IOException {
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByName(testFolder, expectedFolder, "scss", "css", rubySassCss);
  }

  @Test
  public void executeMultipleTimesDoesntThrowOutOfMemoryException() {
    RubySassEngine engine = new RubySassEngine();
    for (int i = 0; i < 100; i++) {
      engine.process("#navbar {width: 80%;}");
    }
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          rubySassCss.process(new StringReader("#navbar {width: 80%;}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }

  /**
   * Test that processing invalid sass css produces exceptions
   *
   * @throws Exception
   */
  @Test
  public void shouldFailWhenInvalidSassCssIsProcessed() throws Exception {
    final File testFolder = new File(url.getFile(), "invalid");
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          rubySassCss.process(null, new FileReader(input), new StringWriter());
          Assert.fail("Expected to fail, but didn't");
        } catch (final WroRuntimeException e) {
          //expected to throw exception, continue
        }
        return null;
      }
    });
  }

}
