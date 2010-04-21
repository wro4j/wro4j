/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;


/**
 * A processor using lessCss engine: @see http://www.asual.com/lesscss/
 * <p>
 * The main css goodies are: <br/>
 * <ul>
 * <li>
 * Variables - Variables allow you to specify widely used values in a single place, and then re-use them throughout the
 * style sheet, making global changes as easy as changing one line of code.<br/>
 *
 * <pre>
 * @brand_color: #4D926F;
 * #header { color: @brand_color; }
 * h2 { color: @brand_color; }
 * </pre>
 * </li>
 * <li>
 * Mixins - Mixins allow you to embed all the properties of a class into another class by simply including the class
 * name as one of its properties. It's just like variables, but for whole classes. Mixins can also behave like
 * functions, and take arguments, as seen in the example bellow.</br>
 *
 * <pre>
 *  .rounded_corners (@radius: 5px) {
 *   -moz-border-radius: @radius;
 *   -webkit-border-radius: @radius;
 *   border-radius: @radius;
 * }
 *
 * #header {
 *   .rounded_corners;
 * }
 *
 * #footer {
 *   .rounded_corners(10px);
 * }
 *  </pre>
 * </li>
 * <li>Nested Rules - Rather than constructing long selector names to specify inheritance, in Less you can simply nest
 * selectors inside other selectors. This makes inheritance clear and style sheets shorter </br>
 *
 * <pre>
 * #header {
 *   color: red;
 *   a {
 *     font-weight: bold;
 *     text-decoration: none;
 *   }
 * }
 * </pre>
 * </li>
 * <li>
 * Operations - Are some elements in your style sheet proportional to other elements? Operations let you add, subtract,
 * divide and multiply property values and colors, giving you the power to do create complex relationships between
 * properties.</br>
 *
 * <pre>
 *  @the-border: 1px;
 * @base-color: #111;
 *
 * #header {
 *   color: @base-color * 3;
 *   border-left: @the-border;
 *   border-right: @the-border * 2;
 * }
 *
 * #footer {
 *   color: (@base-color + #111) * 1.5;
 * }
 *  </pre>
 * </li>
 * </ul>
 * If processing encounter any issues during processing, no change will be applied to the resource.
 * <p/>
 *
 * @author Alex Objelean
 * @created 21 Apr 2010
 */
@SupportedResourceType(ResourceType.CSS)
public class LessCssProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LessCssProcessor.class);
  /**
   * Engine.
   */
  private final LessEngine engine;


  public LessCssProcessor() {
    engine = new LessEngine();
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(engine.compile(content));
    } catch (final LessException e) {
      writer.write(content);
      LOG.warn("Exception while applying lessCss processor on the resource, no processing applied...", e);
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
