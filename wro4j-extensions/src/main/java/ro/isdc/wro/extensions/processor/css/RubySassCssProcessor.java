/*
 * Copyright (c) 2012. All rights reserved.
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
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * A processor using the ruby sass engine:
 *
 * @author Simon van der Sluis
 * @since 1.4.6
 */
@SupportedResourceType(ResourceType.CSS)
public class RubySassCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(RubySassCssProcessor.class);
  public static final String ALIAS = "rubySassCss";
  private ObjectPoolHelper<RubySassEngine> enginePool;

  public RubySassCssProcessor() {
    enginePool = new ObjectPoolHelper<RubySassEngine>(new ObjectFactory<RubySassEngine>() {
      @Override
      public RubySassEngine create() {
        return newEngine();
      }
    });
  }

  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {

		synchronized (this) {

			final String content = IOUtils.toString(reader);
			final RubySassEngine engine = enginePool.getObject();

			try (reader; writer) {
				writer.write(engine.process(content));
			} catch (final WroRuntimeException e) {

				onException(e);
				final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
				LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
						+ " resource, no processing applied...", e);
			} finally {
				enginePool.returnObject(engine);
			}
		}
  }

  /**
   * Invoked when a processing exception occurs. By default propagates the runtime exception.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }

  /**
   * @return a fresh instance of {@link RubySassEngine}
   */
  protected RubySassEngine newEngine() {
    return new RubySassEngine();
  }

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  @Override
  public void destroy() throws Exception {
    enginePool.destroy();
  }
}
