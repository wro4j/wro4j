/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.annot.SupportedResourceType;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.resource.ResourceType;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;


/**
 * YUICssCompressorProcessor. Use YUI css compression utility for processing a css resource.
 *
 * @author Alex Objelean
 * @created Created on Dec 4, 2008
 */
@SupportedResourceType(type=ResourceType.JS)
public class YUIJsCompressorProcessor
  implements ResourcePostProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(YUIJsCompressorProcessor.class);

  // options of YUI compressor
  private final int linebreakpos = -1;

  boolean munge = true;

  boolean preserveAllSemiColons = true;

  boolean disableOptimizations = false;

  boolean verbose = true;

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    final JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new ErrorReporter() {
      public void warning(final String message, final String sourceName, final int line, final String lineSource,
        final int lineOffset) {
        if (line < 0) {
          log.warn("\n[WARNING] " + message);
        } else {
          log.warn("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
        }
      }


      public void error(final String message, final String sourceName, final int line, final String lineSource,
        final int lineOffset) {
        if (line < 0) {
          log.error("\n[ERROR] " + message);
        } else {
          log.error("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
        }
      }


      public EvaluatorException runtimeError(final String message, final String sourceName, final int line,
        final String lineSource, final int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        return new EvaluatorException(message);
      }
    });
    compressor.compress(writer, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations);
  }
}
