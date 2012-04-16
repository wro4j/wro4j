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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
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
  @Inject
  private WroConfiguration configuration;
  private String encoding;
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    try {
      final InputStream is = new ProxyInputStream(new ReaderInputStream(reader, getEncoding())) {};
      final OutputStream os = new ProxyOutputStream(new WriterOutputStream(writer, getEncoding()));
      final JSMin jsmin = new JSMin(is, os);

      jsmin.jsmin();
      is.close();
      os.close();
		} catch (final Exception e) {
		  LOG.error("Exception occured while using processor: " + ALIAS, e);
      throw new IOException(e.getMessage());
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

  /**
   * @return the encoding
   */
  private String getEncoding() {
    return this.encoding == null ? configuration.getEncoding() : encoding;
  }

  /**
   * @param encoding the encoding to set
   */
  public JSMinProcessor setEncoding(final String encoding) {
    this.encoding = encoding;
    return this;
  }
}
