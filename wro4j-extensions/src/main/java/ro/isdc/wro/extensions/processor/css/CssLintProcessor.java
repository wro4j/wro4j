/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.algorithm.csslint.CssLint;
import ro.isdc.wro.extensions.processor.algorithm.csslint.CssLintException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;


/**
 * Processor which analyze the css code and warns you found problems. The processing result won't change no matter if
 * the processed script contains errors or not. The underlying implementation uses CSSLint script utility {@link https
 * ://github.com/stubbornella/csslint}.
 *
 * @author Alex Objelean
 * @since 1.3.8
 * @created 19 Jun 2011
 */
@SupportedResourceType(ResourceType.CSS)
public class CssLintProcessor
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssLintProcessor.class);
  public static final String ALIAS = "cssLint";
  /**
   * Options to use to configure jsHint.
   */
  private String[] options;


  public CssLintProcessor setOptions(final String[] options) {
    this.options = options;
    return this;
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      new CssLint().setOptions(options).validate(content);
    } catch (final CssLintException e) {
      try {
        onCssLintException(e, resource);
      } catch (final Exception ex) {
        throw new WroRuntimeException("", ex);
      }
    } catch (final WroRuntimeException e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      // don't change the processed content no matter what happens.
      writer.write(content);
      reader.close();
      writer.close();
    }
  }

  /**
   * Called when {@link CssLintException} is thrown. Allows subclasses to re-throw this exception as a
   * {@link RuntimeException} or handle it differently. The default implementation simply logs the errors.
   *
   * @param e {@link CssLintException} which has occurred.
   * @param resource the processed resource which caused the exception.
   */
  protected void onCssLintException(final CssLintException e, final Resource resource)
    throws Exception {
    LOG.error("The following resource: " + resource + " has " + e.getErrors().size() + " errors.", e);
  }
}
