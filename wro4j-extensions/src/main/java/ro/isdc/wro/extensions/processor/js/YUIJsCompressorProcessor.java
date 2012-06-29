/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StopWatch;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;


/**
 * YUIJsCompressorProcessor - an adapter for YUI js compression utility for processing js resources.
 *
 * @author Alex Objelean
 * @created Created on Dec 4, 2008
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class YUIJsCompressorProcessor
  implements ResourcePostProcessor, ResourcePreProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(YUIJsCompressorProcessor.class);
  public static final String ALIAS_NO_MUNGE = "yuiJsMin";
  public static final String ALIAS_MUNGE = "yuiJsMinAdvanced";

  /**
   * Error reporter.
   */
  private static final class YUIErrorReporter
    implements ErrorReporter {
    public void warning(final String message, final String sourceName, final int line, final String lineSource,
      final int lineOffset) {
      if (line < 0) {
        LOG.warn("\n[WARNING] " + message);
      } else {
        LOG.warn("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
      }
    }


    public void error(final String message, final String sourceName, final int line, final String lineSource,
      final int lineOffset) {
      if (line < 0) {
        LOG.error("\n[ERROR] " + message);
      } else {
        LOG.error("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
      }
    }


    public EvaluatorException runtimeError(final String message, final String sourceName, final int line,
      final String lineSource, final int lineOffset) {
      error(message, sourceName, line, lineSource, lineOffset);
      return new EvaluatorException(message);
    }
  }

  // options of YUI compressor
  private final int linebreakpos = -1;
  /**
   * Renames variables.
   */
  boolean munge = false;
  boolean verbose = false;
  boolean preserveAllSemiColons = true;
  boolean disableOptimizations = false;

  /**
   * Allows creation of compressor specifying if the munge should apply or not.
   * @param munge
   */
  private YUIJsCompressorProcessor(final boolean munge) {
    this.munge = munge;
  }

  public static YUIJsCompressorProcessor doMungeCompressor() {
    return new YUIJsCompressorProcessor(true);
  }

  public static YUIJsCompressorProcessor noMungeCompressor() {
    return new YUIJsCompressorProcessor(false);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final StopWatch watch = new StopWatch();
    watch.start("pack");
    final String content = IOUtils.toString(reader);
    try {
      final JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(content), new YUIErrorReporter());
      compressor.compress(writer, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations);
    } catch (final Exception e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      //keep js unchanged if it contains errors -> this should be configurable
      LOG.error("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
      onException(new WroRuntimeException("Exception during processing", e));
    } finally {
      reader.close();
      writer.close();
      watch.stop();
      LOG.debug(watch.prettyPrint());
    }
  }
  

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }
}
