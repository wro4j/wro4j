/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.extensions.AbstractWroTest;
import ro.isdc.wro.test.util.ResourceProcessor;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * TestGoogleClosureCompressorProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestGoogleClosureCompressorProcessor extends AbstractWroTest {
  @Test
  public void testDefault()
    throws IOException {
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/googleClosure-input.js",
      "classpath:ro/isdc/wro/extensions/processor/googleClosure-output.js", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          new GoogleClosureCompressorProcessor().process(reader, writer);
        }
      });
  }


  @Test
  public void testAdvanced()
    throws IOException {
    compareProcessedResourceContents("classpath:ro/isdc/wro/extensions/processor/googleClosure-input.js",
      "classpath:ro/isdc/wro/extensions/processor/googleClosure-advanced-output.js", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS).process(reader, writer);
        }
      });
  }

}
