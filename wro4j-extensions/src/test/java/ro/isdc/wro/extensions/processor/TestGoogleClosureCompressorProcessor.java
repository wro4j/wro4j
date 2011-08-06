/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;


/**
 * Test google closure js processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 18, 2010
 */
public class TestGoogleClosureCompressorProcessor {
  private GoogleClosureCompressorProcessor processor;
  @Before
  public void setUp() {
    processor = new GoogleClosureCompressorProcessor() {
      @Override
      protected CompilerOptions newCompilerOptions() {
        final CompilerOptions options = super.newCompilerOptions();
        // explicitly set this to null to make test pass also when running mvn test from command line.
        options.setOutputCharset(null);
        return options;
      }
    };
    Context.set(Context.standaloneContext());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testWhiteSpaceOnly()
      throws IOException {
    processor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
    final URL url = getClass().getResource("google");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedWhitespaceOnly");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", (ResourcePreProcessor) processor);
  }

  @Test
  public void testSimpleOptimization()
      throws IOException {
    processor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
    final URL url = getClass().getResource("google");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedSimple");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", ( ResourcePreProcessor) processor);
  }

  @Test
  public void testAdvancedOptimization()
      throws IOException {
    processor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
    final URL url = getClass().getResource("google");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedAdvanced");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      (ResourcePreProcessor)processor);
  }
}
