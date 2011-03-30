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
  public void testSimpleFromFolder()
    throws IOException {
    final ResourcePostProcessor processor = new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS);

    final URL url = getClass().getResource("google");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "js", "simple.js", WroUtil.newResourceProcessor(processor));
  }

  @Test
  public void testAdvancedFromFolder()
    throws IOException {
    final ResourcePostProcessor processor = new GoogleClosureCompressorProcessor(CompilationLevel.SIMPLE_OPTIMIZATIONS);

    final URL url = getClass().getResource("google");
    final File sourceFolder = new File(url.getFile());
    WroTestUtils.compareSameFolderByExtension(sourceFolder, "js", "advanced.js", WroUtil.newResourceProcessor(processor));
  }
}
