/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.fail;
import static ro.isdc.wro.util.WroTestUtils.runConcurrently;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test ruby sass css processor.
 *
 * @author Simon van der Sluis
 */
public class TestRubySassCssProcessor {
  /** Location (base) of ruby sass css test resources. */
  private final URL url = getClass().getResource("rubysasscss");

  /** A RubySassEngine to test */
  private RubySassCssProcessor processor;

  @Before
  public void setUp() {
    processor = new RubySassCssProcessor();
  }

  @Test
  public void shouldTestFromFolder()
      throws Exception {
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByName(testFolder, expectedFolder, "css", "css", processor);
  }

  @Test
  public void executeMultipleTimesDoesntThrowOutOfMemoryException() {
    final RubySassEngine engine = new RubySassEngine();
    for (int i = 0; i < 100; i++) {
      engine.process("#navbar {width: 80%;}");
    }
  }

  @Test
  public void shouldBeThreadSafe()
      throws Exception {
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(new StringReader(
              "$side: top;$radius: 10px;.rounded-#{$side} {border-#{$side}-radius: $radius;}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    runConcurrently(task);
  }

  /**
   * Test ruby sass css processor with multi-threads.
   *
   * @author Simon van der Sluis
   */
  @Test
  public void shouldBeThreadSafeWhenInitializingProcessor()
          throws Exception {
    final RubySassCssProcessor processor = new RubySassCssProcessor();
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          // This should be called first time in multi-thread.
          processor.process(new StringReader(
                  "$side: top;$radius: 10px;.rounded-#{$side} {border-#{$side}-radius: $radius;}"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    runConcurrently(task);
  }


  /**
   * Test that processing invalid sass css produces exceptions.
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
          processor.process(null, new FileReader(input), new StringWriter());
          fail("Shouldn't have failed");
        } catch (final WroRuntimeException e) {
          // expected to throw exception, continue
        }
        return null;
      }
    });
  }

  /**
   * This test proves that Sass Engine behave strangely after the first failure.
   */
  @Test
  public void shouldSucceedAfterAFailure()
      throws Exception {
    try {
      processor.process(null, new StringReader("$base= #f938ab;"), new StringWriter());
      fail("Should have failed");
    } catch (final Exception e) {

    }
    final String sass = ".valid {color: red}  @mixin rounded($side, $radius: 10px) { border-#{$side}-radius: $radius; -moz-border-radius-#{$side}: $radius; -webkit-border-#{$side}-radius: $radius;}#navbar li { @include rounded(top); }";
    processor.process(null, new StringReader(sass), new StringWriter());
  }

}
