/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import javax.servlet.ServletContext;


/**
 * A processor using lessCss engine: @see http://www.asual.com/lesscss/
 * <p>
 * The main css goodies are: <br/>
 * <ul>
 * <li>Variables - Variables allow you to specify widely used values in a single place, and then re-use them throughout
 * the style sheet, making global changes as easy as changing one line of code.<br/>
 * 
 * <pre>
 * @brand_color: #4D926F;
 * #header { color: @brand_color; }
 * h2 { color: @brand_color; }
 * </pre>
 * 
 * </li>
 * <li>Mixins - Mixins allow you to embed all the properties of a class into another class by simply including the class
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
 * </pre>
 * 
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
 * 
 * </li>
 * <li>Operations - Are some elements in your style sheet proportional to other elements? Operations let you add,
 * subtract, divide and multiply property values and colors, giving you the power to do create complex relationships
 * between properties.</br>
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
 * </pre>
 * 
 * </li>
 * </ul>
 * If processing encounter any issues during processing, no change will be applied to the resource.
 * <p/>
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 10 Sep 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class NodeLessCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(NodeLessCssProcessor.class);
  
  public static final String ALIAS = "nodeLessCss";
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      String resourceUri = resource == null ? "unknown.less" : resource.getUri();
      writer.write(process(resourceUri, content));
    } catch (final WroRuntimeException e) {
        final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
        LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
                + " resource, no processing applied...", e);
      onException(e);
    } finally {
      // return for later reuse
      reader.close();
      writer.close();
    }
  }
  
  private String process(final String resourceUri, final String content) {
    InputStream shellIn = null;
    File temp = null; 
    try {
      temp = createTempFile();
      //FileUtils.writeStringToFile(temp, content);
      IOUtils.write(content, new FileOutputStream(temp), "UTF-8");
      LOG.debug("absolute path: " + temp.getAbsolutePath());
      final String filePath = temp.getPath();
      ProcessBuilder pb = new ProcessBuilder("lessc", filePath);
      pb.redirectErrorStream(true);
      Process shell = pb.start();
      shellIn = shell.getInputStream();
      final String result = IOUtils.toString(shellIn, "UTF-8");
      int exitStatus = shell.waitFor();//this won't return till `out' stream being flushed!
      if (exitStatus != 0) {
        final String errorMessage = MessageFormat.format("Error in LESS file: {0}\n\n{1}", temp, result.replace(filePath, resourceUri));
        throw new WroRuntimeException(errorMessage);
      }
      LOG.debug("exitStatus: {}", exitStatus);
      LOG.debug(result);
      FileUtils.deleteQuietly(temp);
      return result;
    } catch (Exception e) {
      LOG.error(e.getMessage());
      throw new WroRuntimeException(e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(shellIn);
    }
  }

  private File createTempFile() {
    return new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".less");
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
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
  
}
