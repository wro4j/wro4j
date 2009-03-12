/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * YUICssCompressorProcessor. Use YUI css compression utility for processing a
 * css resource.
 * 
 * @author Alexandru.Objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Dec 4, 2008
 */
public class YUIJsCompressorProcessor implements ResourcePreProcessor,
    ResourcePostProcessor {
  // options of YUI compressor
  private final int linebreakpos = -1;

  boolean munge = true;

  boolean preserveAllSemiColons = true;

  boolean disableOptimizations = false;

  boolean verbose = true;

  /**
   * {@inheritDoc}
   */
  public void process(final String resourceUri, final Reader reader,
      final Writer writer) throws IOException {
    // resourceUri doesn't matter
    this.process(reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    final JavaScriptCompressor compressor = new JavaScriptCompressor(reader,
        new ErrorReporter() {
          public void warning(String message, String sourceName, int line,
              String lineSource, int lineOffset) {
            if (line < 0) {
              System.err.println("\n[WARNING] " + message);
            } else {
              System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':'
                  + message);
            }
          }

          public void error(String message, String sourceName, int line,
              String lineSource, int lineOffset) {
            if (line < 0) {
              System.err.println("\n[ERROR] " + message);
            } else {
              System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':'
                  + message);
            }
          }

          public EvaluatorException runtimeError(String message,
              String sourceName, int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
          }
        });
    compressor.compress(writer, linebreakpos, munge, verbose,
        preserveAllSemiColons, disableOptimizations);
  }
}
