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


/**
 * Important node: this processor is not cross platform and has some pre-requesites in order to work.
 * <p/>
 * Same as {@link LessCssProcessor} but uses <code>lessc</code> shell utility to process the less.
 * <p/>
 * Installation instructions: Install the libnode-less package (Unix OS)
 * 
 * <pre>
 *   sudo apt-get install libnode-less
 * </pre>
 * 
 * It is possible to test whether the lessc utility is available using {@link NodeLessCssProcessor#isSupported()}
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 10 Sep 2012
 */
@SupportedResourceType(ResourceType.CSS)
public class NodeLessCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final String SHELL_COMMAND = "lessc";

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
      final String resourceUri = resource == null ? "unknown.less" : resource.getUri();
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
    //the file holding the input file to process
    File temp = null; 
    try {
      temp = createTempFile();
      final String encoding = "UTF-8";
      IOUtils.write(content, new FileOutputStream(temp), encoding);
      LOG.debug("absolute path: {}", temp.getAbsolutePath());
      final String tempFilePath = temp.getPath();
      final ProcessBuilder processBuilder = new ProcessBuilder(SHELL_COMMAND, tempFilePath).redirectErrorStream(true);
      final Process shell = processBuilder.start();
      shellIn = shell.getInputStream();
      int exitStatus = shell.waitFor();// this won't return till `out' stream being flushed!
      final String result = IOUtils.toString(shellIn, encoding);
      if (exitStatus != 0) {
        LOG.error("exitStatus: {}", exitStatus);
        //find a way to get rid of escape character found at the end (minor issue)
        final String errorMessage = MessageFormat.format("Error in LESS: \n{0}", result.replace(tempFilePath, resourceUri));
        throw new WroRuntimeException(errorMessage);
      }
      return result;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new WroRuntimeException(e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(shellIn);
      //always cleanUp
      FileUtils.deleteQuietly(temp);
    }
  }
  
  /**
   * @return true if the processor is supported on this environment. The implementation check if the required shell
   *         utility is available.
   */
  public boolean isSupported() {
    try {
      new ProcessBuilder(SHELL_COMMAND).start();
      return true;
    } catch (Exception e) {
      return false;
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
