/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * Test google closure js processor.
 *
 * @author Alex Objelean
 * @created Created on Apr 18, 2010
 */
public class TestGoogleClosureCompressorProcessor {
  @Test
  public void testWhiteSpaceOnly() throws IOException {
    final ResourcePostProcessor processor = new GoogleClosureCompressorProcessor(CompilationLevel.WHITESPACE_ONLY);
    final URL url = getClass().getResource("google");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedWhitespaceOnly");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      WroUtil.newResourceProcessor(processor));
  }

  @Test
  public void testSimpleOptimization() throws IOException {
    final ResourcePostProcessor processor = new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS);
    final URL url = getClass().getResource("google");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedSimple");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      WroUtil.newResourceProcessor(processor));
  }

  @Test
  public void testAdvancedOptimization() throws IOException {
    final ResourcePostProcessor processor = new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS);
    final URL url = getClass().getResource("google");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expectedAdvanced");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js",
      WroUtil.newResourceProcessor(processor));
  }
}
