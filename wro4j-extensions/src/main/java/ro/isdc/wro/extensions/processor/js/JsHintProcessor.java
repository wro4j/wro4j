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
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Processor which analyze the js code and warns you about any problems.
 *
 * @author Alex Objelean
 * @created 31 Jul 2010
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class JsHintProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(JsHintProcessor.class);

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      new JsHint().validate(content);
      writer.write(content);
    } catch (final JsHintException e) {
      //TODO leave the script the same when error occurs?
      LOG.error("The processed script has errors, the processed content will be empty.");
    } catch (final WroRuntimeException e) {
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }

}
