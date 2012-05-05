/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorDecorator;

/**
 * Enforce decorated processors to be applied only on predefined extension. The extenions should be of this form: "js",
 * "coffee", "css", etc. Usage example:
 * <p/>
 * <code>
 * ExtensionsAwareProcessorDecorator.decorate(decoratedProcessor).addExtension("js");
 * </code>
 *
 * @author Alex Objelean
 * @created 16 Sep 2011
 * @since 1.4.1
 */
public class ExtensionsAwareProcessorDecorator
    extends ProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(ExtensionsAwareProcessorDecorator.class);
  /**
   * Set of extensions  on which the decorated processor will be applied.
   */
  private final Set<String> extensions = new HashSet<String>();

  private ExtensionsAwareProcessorDecorator(final ResourceProcessor preProcessor) {
    super(preProcessor);
  }

  /**
   * Add one more extension to the set of extensions.
   *
   * @param extension to add.
   */
  public ExtensionsAwareProcessorDecorator addExtension(final String extension) {
    Validate.notBlank(extension);
    extensions.add(extension);
    return this;
  }

  public static ExtensionsAwareProcessorDecorator decorate(final ResourceProcessor preProcessor) {
    return new ExtensionsAwareProcessorDecorator(preProcessor);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    if (resource != null) {
      final String resourceExtension = FilenameUtils.getExtension(resource.getUri());
      if (extensions.contains(resourceExtension)) {
        LOG.debug("[OK] Process resource {} with extension: {}", resource.getUri(), resourceExtension);
        getDecoratedObject().process(resource, reader, writer);
      } else {
        LOG.debug("[SKIP] Process resource with extension: {}", resource.getUri(), resourceExtension);
        IOUtils.copy(reader, writer);
      }
    } else {
      LOG.debug("When used as a postProcessor, decorated processor is applied");
      getDecoratedObject().process(resource, reader, writer);
    }
  }

}
