/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.decorator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


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
   * Set of extensions on which the decorated processor will be applied.
   */
  private final Set<String> extensions = new HashSet<String>();

  private ExtensionsAwareProcessorDecorator(final ResourcePreProcessor preProcessor) {
    super(preProcessor);
  }

  private ExtensionsAwareProcessorDecorator(final ResourcePostProcessor postProcessor) {
    super(postProcessor);
  }

  /**
   * Add one more extension to the set of extensions.
   *
   * @param extension
   *          to add.
   */
  public ExtensionsAwareProcessorDecorator addExtension(final String extension) {
    Validate.notBlank(extension);
    extensions.add(extension);
    return this;
  }

  public static ExtensionsAwareProcessorDecorator decorate(final ResourcePreProcessor preProcessor) {
    return new ExtensionsAwareProcessorDecorator(preProcessor);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isEnabled(final Resource resource) {
    return super.isEnabled(resource) && isApplicable(resource);
  }

  /**
   * @param resource
   *          {@link Resource} being processed.
   * @return true if the processed resource has one of the accepted extension.
   */
  private boolean isApplicable(final Resource resource) {
    final String resourceExtension = FilenameUtils.getExtension(resource.getUri());
    // null resource also means that processor is applicable, since we cannot check the extension (postProcessor).
    final boolean isApplicable = resource == null || extensions.contains(resourceExtension);
    if (isApplicable) {
      LOG.debug("[OK] Process resource {} with extension: {}", resource.getUri(), resourceExtension);
    }
    return isApplicable;
  }
}
