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
 * Processor which analyze the js code and warns you about any problems.
 *
 * @author Alex Objelean
 * @created 31 Jul 2010
 */
@SupportedResourceType(ResourceType.JS)
public class JsHintPreProcessor
  implements ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(JsHintPreProcessor.class);

  private String[] options;


  public JsHintPreProcessor setOptions(final String[] options) {
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
      writer.write(content);
    } catch (final JsHintException e) {
      try {
        LOG.error("The following resource: " + resource + " has errors.", e);
        // TODO leave the script the same when error occurs?
        onJsHintException(e, resource);
      } catch (final Exception ex) {
        throw new WroRuntimeException("", ex);
      }
    } catch (final WroRuntimeException e) {
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * Called when {@link JsHintException} is thrown. Allows subclasses to re-throw this exception as a
   * {@link RuntimeException} or handle it differently.
   *
   * @param e {@link JsHintException} which has occurred.
   * @param resource the processed resource which caused the exception.
   */
  protected void onJsHintException(final JsHintException e, final Resource resource) throws Exception {}
}
