/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.css.BourbonCssProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test BourbonCssProcessor.
 *
 * @author Simon van der Sluis
 */
public class TestBourbonCssProcessor {

  /** Location (base) of bourbon sass css with bourbon test resources. */
  private final URL url = getClass().getResource("bourboncss");

  /** A RubySassEngine to test */
  private BourbonCssProcessor processor;

  @Before
  public void initEngine() {
    processor = new BourbonCssProcessor();
  }

  /**
   * Ignore this test since it doesn't pass consistently because of some internal error of jruby.
   * The error is that the paths used by the sass import directive are based on the current working dir.
   * This test will pass when run from the wro4j-extensions module, but error if run from the parent (wro4j) project.
   * It's impossible to change the current working dir from within a running JVM, and I cannot find a maven directive
   * to make module rnu tests from their module dir, so no fix seems likely.
   * They can however be run manually from and IDE if the working dir is correctly configured, so are still useful
   * when making changes to the bourbon sass processor.
   */
  @Ignore
  @Test
  public void testFromFolder()
      throws Exception {
    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByName(testFolder, expectedFolder, "scss", "css", processor);
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    final Callable<Void> task = new Callable<Void>() {
      @Override
      public Void call() {
        try {
          processor.process(new StringReader("h3#heading {  font-size: modular-scale(14px, 1, 1.618); }"), new StringWriter());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    };
    WroTestUtils.runConcurrently(task);
  }


  @Test
  public void shouldSupportOnlyCssResources() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }
}
