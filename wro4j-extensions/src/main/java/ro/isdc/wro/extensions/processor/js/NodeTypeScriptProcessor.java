/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;
import ro.isdc.wro.util.WroUtil;


/**
 * Important node: this processor is not cross platform and has some pre-requesites in order to work.
 * <p/>
 * Installation instructions: Install the typescript node package module (Unix OS)
 *
 * <pre>
 *    npm install -g typescript
 * </pre>
 *
 * It is possible to test whether the tsc utility is available using {@link NodeTypeScriptProcessor#isSupported()}
 *
 * @author Alex Objelean
 * @since 1.5.0
 * @created 10 Sep 2012
 */
@SupportedResourceType(ResourceType.JS)
public class NodeTypeScriptProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, SupportAware {
  private static final String TYPESCRIPT_EXTENSION = "ts";
  private static final String SHELL_COMMAND = "tsc";
  private static final String ARG_OUT = "--out";
  private static final Logger LOG = LoggerFactory.getLogger(NodeTypeScriptProcessor.class);
  public static final String ALIAS = "nodeTypeScript";
  /**
   * Flag indicating that we are running on Windows platform. This will be initialized only once in constructor.
   */
  private final boolean isWindows;

  public NodeTypeScriptProcessor() {
    // initialize this field at construction.
    final String osName = System.getProperty("os.name");
    LOG.debug("OS Name: {}", osName);
    isWindows = osName != null && osName.contains("Windows");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    final String resourceUri = resource == null ? "unknown.js" : resource.getUri();
    try {
      writer.write(process(resourceUri, content));
    } catch (final Exception e) {
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
      LOG.error(e.getMessage(), e);
      onException(e, content);
    } finally {
      // return for later reuse
      reader.close();
      writer.close();
    }
  }

  private String process(final String resourceUri, final String content) {
    final InputStream shellIn = null;
    // the file holding the input file to process
    File tempSource = null;
    File tempDest = null;
    try {
      tempSource = WroUtil.createTempFile(TYPESCRIPT_EXTENSION);
      tempDest = WroUtil.createTempFile(TYPESCRIPT_EXTENSION);
      final String encoding = "UTF-8";
      IOUtils.write(content, new FileOutputStream(tempSource), encoding);
      LOG.debug("absolute path: {}", tempSource.getAbsolutePath());

      final Process process = createProcess(tempSource, tempDest);
      final int exitStatus = process.waitFor();// this won't return till `out' stream being flushed!
      final String result = IOUtils.toString(new FileInputStream(tempDest), encoding);
      if (exitStatus != 0) {
        LOG.error("exitStatus: {}", exitStatus);
        String errorMessage = IOUtils.toString(process.getInputStream(), encoding);
        // find a way to get rid of escape character found at the end (minor issue)
        errorMessage = MessageFormat.format("Error in Typescript: \n{0}",
            errorMessage.replace(tempSource.getPath(), resourceUri));
        throw new WroRuntimeException(errorMessage).logError();
      }
      return result;
    } catch (final Exception e) {
      throw WroRuntimeException.wrap(e);
    } finally {
      IOUtils.closeQuietly(shellIn);
      // always cleanUp
      FileUtils.deleteQuietly(tempSource);
      FileUtils.deleteQuietly(tempDest);
    }
  }

  /**
   * Invoked when a processing exception occurs. Default implementation wraps the original exception into
   * {@link WroRuntimeException}.
   *
   * @param e
   *          the {@link Exception} thrown during processing
   * @param content
   *          the resource content being processed.
   */
  protected void onException(final Exception e, final String content) {
    throw WroRuntimeException.wrap(e);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  /**
   * Creates process responsible for running tsc shell command by reading the file content from the sourceFilePath
   *
   * @param sourceFile
   *          the source path of the file from where the tsc will read the typescript file.
   * @param destFile
   *          the destination of the compiled file.
   * @throws IOException
   *           when the process execution fails.
   */
  private Process createProcess(final File sourceFile, final File destFile)
      throws IOException {
    notNull(sourceFile);
    final String[] commandLine = getCommandLine(sourceFile.getPath(), destFile.getPath());
    LOG.debug("CommandLine arguments: {}", Arrays.asList(commandLine));
    return new ProcessBuilder(commandLine).redirectErrorStream(true).start();
  }

  /**
   * @return true if the processor is supported on this environment. The implementation check if the required shell
   *         utility is available.
   */
  @Override
  public boolean isSupported() {
    File tempSource = null;
    File tempDest = null;
    try {
      tempSource = WroUtil.createTempFile(TYPESCRIPT_EXTENSION);
      tempDest = WroUtil.createTempFile(TYPESCRIPT_EXTENSION);
      final Process process = createProcess(tempSource, tempDest);
      final int exitValue = process.waitFor();
      LOG.debug("exitValue {}. ErrorMessage: {}", exitValue, IOUtils.toString(process.getInputStream()));
      if (exitValue != 0) {
        throw new UnsupportedOperationException("Tsc is not a supported operation on this platform");
      }
      LOG.debug("The {} processor is supported.", getClass().getName());
      return true;
    } catch (final Exception e) {
      LOG.debug("Unsupported processor", e);
      LOG.warn("The {} processor is not supported. Because: {}", getClass().getName(), e.getMessage());
      return false;
    } finally {
      FileUtils.deleteQuietly(tempSource);
      FileUtils.deleteQuietly(tempDest);
    }
  }

  /**
   * Creates the platform specific arguments to run the <code>tsc</code> shell utility. Default implementation handles
   * windows and unix platforms.
   *
   * @return arguments for command line. The implementation will take care of OS differences.
   */
  protected String[] getCommandLine(final String filePath, final String outFilePath) {
    return isWindows ? buildArgumentsForWindows(filePath, outFilePath) : buildArgumentsForUnix(filePath, outFilePath);
  }

  /**
   * @return arguments required to run tsc on non Windows platform.
   */
  private String[] buildArgumentsForUnix(final String filePath, final String outFilePath) {
    return new String[] {
      SHELL_COMMAND, filePath, ARG_OUT, outFilePath
    };
  }

  /**
   * @return arguments required to run tsc on Windows platform.
   */
  private String[] buildArgumentsForWindows(final String filePath, final String outFilePath) {
    return new String[] {
      "cmd", "/c", SHELL_COMMAND, filePath, ARG_OUT, outFilePath
    };
  }
}
