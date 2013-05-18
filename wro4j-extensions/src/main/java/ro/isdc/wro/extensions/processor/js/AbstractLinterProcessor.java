/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.linter.AbstractLinter;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Processor which analyze the js code and warns you about any problems. The processing result won't change no matter
 * if the processed script contains errors or not.
 *
 * @author Alex Objelean
 * @since 1.3.5
 * @created 1 Mar 2011
 */
@SupportedResourceType(ResourceType.JS)
public abstract class AbstractLinterProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractLinterProcessor.class);
  private ObjectPoolHelper<AbstractLinter> enginePool;
  /**
   * Options to use to configure the linter.
   */
  private String options;

  public AbstractLinterProcessor() {
    enginePool = new ObjectPoolHelper<AbstractLinter>(new ObjectFactory<AbstractLinter>() {
      @Override
      public AbstractLinter create() {
        return newLinter();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    final AbstractLinter linter = enginePool.getObject();
    try {
      // TODO investigate why linter fails when trying to reuse the same instance twice
      linter.setOptions(getOptions()).validate(content);
    } catch (final LinterException e) {
      onLinterException(e, resource);
    } catch (final WroRuntimeException e) {
      onException(e);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      // don't change the processed content no matter what happens.
      writer.write(content);
      reader.close();
      writer.close();
      enginePool.returnObject(linter);
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer) throws IOException {
    process(null, reader, writer);
  }

  /**
   * Called when {@link LinterException} is thrown. Allows subclasses to re-throw this exception as a
   * {@link RuntimeException} or handle it differently. The default implementation simply logs the errors.
   *
   * @param e {@link LinterException} which has occurred.
   * @param resource the processed resource which caused the exception.
   */
  protected void onLinterException(final LinterException e, final Resource resource) {
    LOG.error("The following resource: " + resource + " has " + e.getErrors().size() + " errors.", e);
  }

  /**
   * @return an options as CSV.
   */
  private String getOptions() {
    if (options == null) {
      options = createDefaultOptions();
    }
    return options;
  }

  /**
   * @return default options to use for linting.
   */
  protected String createDefaultOptions() {
    return "";
  }

  /**
   * @param options comma separated list of options.
   */
  public AbstractLinterProcessor setOptionsAsString(final String options) {
    this.options = options;
    return this;
  }

  /**
   * Sets an array of options.
   * Use {@link #setOptionsAsString(String)} instead.
   */
  @Deprecated
  public AbstractLinterProcessor setOptions(final String... options) {
    this.options = StringUtils.join(options, ',');
    LOG.debug("setOptions: {}", this.options);
    return this;
  }

  /**
   * @return the linter to use for js code validation.
   */
  protected abstract AbstractLinter newLinter();
}
