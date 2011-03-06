/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHint;
import ro.isdc.wro.extensions.processor.algorithm.jshint.JsHintException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Processor which analyze the js code and warns you about any problems. The processing result won't change no matter
 * if the processed script contains errors or not.
 *
 * @author Alex Objelean
 * @created 1 Mar 2011
 */
@SupportedResourceType(ResourceType.JS)
public class JsHintProcessor
  implements ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(JsHintProcessor.class);
  /**
   * Options to use to configure jsHint.
   */
  private String[] options;


  public JsHintProcessor setOptions(final String[] options) {
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
      new JsHint().setOptions(options).validate(content);
    } catch (final JsHintException e) {
      try {
        onJsHintException(e, resource);
      } catch (final Exception ex) {
        throw new WroRuntimeException("", ex);
      }
    } catch (final WroRuntimeException e) {
      LOG.warn("Exception while applying " + getClass().getSimpleName()
        + " processor on the resource, no processing applied...", e);
    } finally {
      // don't change the processed content no matter what happens.
      writer.write(content);
      reader.close();
      writer.close();
    }
  }


  /**
   * Called when {@link JsHintException} is thrown. Allows subclasses to re-throw this exception as a
   * {@link RuntimeException} or handle it differently. The default implementation simply logs the errors.
   *
   * @param e {@link JsHintException} which has occurred.
   * @param resource the processed resource which caused the exception.
   */
  protected void onJsHintException(final JsHintException e, final Resource resource)
    throws Exception {
    LOG.error("The following resource: " + resource + " has " + e.getErrors().size() + " errors.", e);
  }
}
