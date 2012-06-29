/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test ruby sass css processor.
 * 
 * @author Simon van der Sluis
 * @created Created on Apr 21, 2010
 */
public class TestRubySassCssProcessor {
  /** Location (base) of ruby sass css test resources. */
  private final URL url = getClass().getResource("rubysasscss");
  
  /** A RubySassEngine to test */
  private RubySassCssProcessor rubySassCss;
  
  @Before
  public void initEngine() {
    rubySassCss = new RubySassCssProcessor();
  }
  
  @Test
  public void shouldProcessResourcesFromFolder()
          throws Exception {
      final URL url = getClass().getResource("rubysass");
      final ResourcePreProcessor processor = new RubySassCssProcessor();

      final File testFolder = new File(url.getFile(), "templates");
      final File expectedFolder = new File(url.getFile(), "results");
      WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
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
  public void shouldBeThreadSafe()
      throws Exception {
    final Callable<Void> task = new Callable<Void>() {
      public Void call() {
        try {
          rubySassCss.process(new StringReader("$side: top;$radius: 10px;.rounded-#{$side} {border-#{$side}-radius: $radius;}"), new StringWriter());
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
  public void shouldFailWhenInvalidSassCssIsProcessed()
      throws Exception {
    final File testFolder = new File(url.getFile(), "invalid");
    WroTestUtils.forEachFileInFolder(testFolder, new Function<File, Void>() {
      @Override
      public Void apply(final File input)
          throws Exception {
        try {
          rubySassCss.process(null, new FileReader(input), new StringWriter());
          Assert.fail("Expected to fail, but didn't");
        } catch (final WroRuntimeException e) {
          // expected to throw exception, continue
        }
        return null;
      }
    });
  }
  
}
