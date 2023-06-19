/*
 * Copyright (C) 2010. All rights reserved.
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
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;


/**
 * A processor using lessCss engine: @see http://www.asual.com/lesscss/
 * <p>
 * The main css goodies are:
 * <ul>
 * <li>Variables - Variables allow you to specify widely used values in a single place, and then re-use them throughout
 * the style sheet, making global changes as easy as changing one line of code.
 * </li>
 * <li>Mixins - Mixins allow you to embed all the properties of a class into another class by simply including the class
 * name as one of its properties. It's just like variables, but for whole classes. Mixins can also behave like
 * functions, and take arguments.
 * </li>
 * <li>Nested Rules - Rather than constructing long selector names to specify inheritance, in Less you can simply nest
 * selectors inside other selectors. This makes inheritance clear and style sheets shorter.
 * </li>
 * <li>Operations - Are some elements in your style sheet proportional to other elements? Operations let you add,
 * subtract, divide and multiply property values and colors, giving you the power to do create complex relationships
 * between properties.
 * </li>
 * </ul>
 * If processing encounter any issues during processing, no change will be applied to the resource.
 *
 * @author Alex Objelean
 * @since 1.2.6
 */
@SupportedResourceType(ResourceType.CSS)
public class RhinoLessCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(RhinoLessCssProcessor.class);
  public static final String ALIAS = "rhinoLessCss";
  private ObjectPoolHelper<LessCss> enginePool;

  public RhinoLessCssProcessor() {
    enginePool = new ObjectPoolHelper<LessCss>(new ObjectFactory<LessCss>() {
      @Override
      public LessCss create() {
        return newLessCss();
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
    final LessCss lessCss = enginePool.getObject();
    try {
      writer.write(lessCss.less(content));
    } catch (final WroRuntimeException e) {
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
      onException(e);
    } finally {
      // return for later reuse
      enginePool.returnObject(lessCss);
      reader.close();
      writer.close();
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
    throw e;
  }

  /**
   * @return the {@link LessCss} engine implementation. Override it to provide a different version of the less.js
   *         library. Useful for upgrading the processor outside the wro4j release.
   */
  protected LessCss newLessCss() {
    return new LessCss();
  }

  /**
   * {@inheritDoc}
   */
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
