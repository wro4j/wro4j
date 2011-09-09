/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.js;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.JSMin;


/**
 * Use JSMin utility for js compression. This processor is annotated with {@link Minimize} because it performs
 * minimization.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class JSMinProcessor implements ResourcePreProcessor,
    ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(JSMinProcessor.class);
  public static final String ALIAS = "jsMin";
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    try {
      final String encoding = Context.get().getConfig().getEncoding();

      final InputStream is = new ProxyInputStream(new ReaderInputStream(reader, encoding)) {};
      final OutputStream os = new ProxyOutputStream(new WriterOutputStream(writer, encoding));
      final JSMin jsmin = new JSMin(is, os);

      jsmin.jsmin();
      is.close();
      os.close();
    } catch (final IOException e) {
      LOG.error("Error occured", e);
      throw e;
		} catch (final Exception e) {
		  final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
		  final String errorMessage = "Exception while applying " + getClass().getSimpleName() + " processor on the "
      + resourceUri + " resource";
		  LOG.error(errorMessage);
      throw new IOException(errorMessage, e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader,
      final Writer writer) throws IOException {
    // resource Uri doesn't matter.
    process(null, reader, writer);
  }
}
