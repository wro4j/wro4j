/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.maven.plugin.MojoExecutionException;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.encoding.SmartEncodingInputStream;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;


/**
 * A build-time solution for organizing and minimizing static resources. By default uses the same configuration as the
 * run-time solution. Additionally, allows you to change the processors used by changing the wroManagerFactory
 * implementation used by the plugin.
 *
 * @goal run
 * @phase process-resources
 * @requiresDependencyResolution runtime
 *
 * @author Alex Objelean
 */
public class Wro4jMojo extends AbstractWro4jMojo {
  /**
   * The path to the destination directory where the files are stored at the end of the process.
   *
   * @parameter default-value="${project.build.directory}/wro/" expression="${destinationFolder}"
   * @optional
   */
  private File destinationFolder;
  /**
   * @parameter expression="${cssDestinationFolder}"
   * @optional
   */
  private File cssDestinationFolder;
  /**
   * @parameter expression="${jsDestinationFolder}"
   * @optional
   */
  private File jsDestinationFolder;
  /**
   * @parameter expression="${wroManagerFactory}"
   * @optional
   */
  private String wroManagerFactory;

  /**
   * {@inheritDoc}
   */
  @Override
  protected StandaloneContextAwareManagerFactory newWroManagerFactory() throws MojoExecutionException {
    if (wroManagerFactory != null) {
      return createCustomManagerFactory();
    }
    return super.newWroManagerFactory();
  }

  /**
   * Creates an instance of Manager factory based on the value of the wroManagerFactory plugin parameter value.
   */
  private StandaloneContextAwareManagerFactory createCustomManagerFactory()
    throws MojoExecutionException {
    StandaloneContextAwareManagerFactory managerFactory;
    try {
      final Class<?> wroManagerFactoryClass = Thread.currentThread().getContextClassLoader().loadClass(
        wroManagerFactory.trim());
      managerFactory = (StandaloneContextAwareManagerFactory)wroManagerFactoryClass.newInstance();
    } catch (final Exception e) {
      getLog().error("Cannot instantiate wroManagerFactoryClass", e);
      throw new MojoExecutionException("Invalid wroManagerFactoryClass, called: " + wroManagerFactory, e);
    }
    return managerFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validate()
    throws MojoExecutionException {
    super.validate();
    //additional validation requirements
    if (destinationFolder == null) {
      throw new MojoExecutionException("destinationFolder was not set!");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute()
    throws Exception {
    getLog().info("destinationFolder: " + destinationFolder);
    getLog().info("jsDestinationFolder: " + jsDestinationFolder);
    getLog().info("cssDestinationFolder: " + cssDestinationFolder);

    final Collection<String> groupsAsList = getTargetGroupsAsList();
    for (final String group : groupsAsList) {
      for (final ResourceType resourceType : ResourceType.values()) {
        final File destinationFolder = computeDestinationFolder(resourceType);
        final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
        processGroup(groupWithExtension, destinationFolder);
      }
    }
  }


  /**
   * Encodes a version using some logic.
   *
   * @param group the name of the resource to encode.
   * @param input the stream of the result content.
   * @return the name of the resource with the version encoded.
   */
  private String rename(final String group, final InputStream input)
    throws Exception {
    try {
      return getManagerFactory().getNamingStrategy().rename(group, input);
    } catch (final IOException e) {
      throw new MojoExecutionException("Error occured during renaming", e);
    }
  }


  /**
   * Computes the destination folder based on resource type.
   *
   * @param resourceType {@link ResourceType} to process.
   * @return destinationFoder where the result of resourceType will be copied.
   * @throws MojoExecutionException if computed folder is null.
   */
  private File computeDestinationFolder(final ResourceType resourceType)
    throws MojoExecutionException {
    File folder = destinationFolder;
    if (resourceType == ResourceType.JS) {
      if (jsDestinationFolder != null) {
        folder = jsDestinationFolder;
      }
    }
    if (resourceType == ResourceType.CSS) {
      if (cssDestinationFolder != null) {
        folder = cssDestinationFolder;
      }
    }
    getLog().info("folder: " + folder);
    if (folder == null) {
      throw new MojoExecutionException("Couldn't compute destination folder for resourceType: " + resourceType
        + ". That means that you didn't define one of the following parameters: "
        + "destinationFolder, cssDestinationFolder, jsDestinationFolder");
    }
    if (!folder.exists()) {
      folder.mkdirs();
    }
    return folder;
  }

  /**
   * Process a single group.
   */
  private void processGroup(final String group, final File parentFoder)
    throws Exception {
    ByteArrayOutputStream resultOutputStream = null;
    InputStream resultInputStream = null;
    try {
      getLog().info("processing group: " + group);

      //mock request
      final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Mockito.when(request.getRequestURI()).thenReturn(group);
      //mock response
      final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
      resultOutputStream = new ByteArrayOutputStream();
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(resultOutputStream));

      //init context
      final WroConfiguration config = Context.get().getConfig();
      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);
      //perform processing
      getManagerFactory().getInstance().process();
      //encode version & write result to file
      resultInputStream = new UnclosableBufferedInputStream(resultOutputStream.toByteArray());
      final File destinationFile = new File(parentFoder, rename(group, resultInputStream));
      destinationFile.createNewFile();
      //allow the same stream to be read again
      resultInputStream.reset();
      getLog().debug("Created file: " + destinationFile.getName());

      final OutputStream fos = new FileOutputStream(destinationFile);
      //use reader to detect encoding
      IOUtils.copy(new SmartEncodingInputStream(resultInputStream), fos);
      fos.close();
      getLog().info("file size: " + destinationFile.getName() + " -> " + destinationFile.length() + " bytes");
      // delete empty files
      if (destinationFile.length() == 0) {
        getLog().info("No content found for group: " + group);
        destinationFile.delete();
      } else {
        getLog().info(destinationFile.getAbsolutePath()
          + " (" + destinationFile.length() + "bytes" + ") has been created!");
      }
    } finally {
      if (resultOutputStream != null) {
        resultOutputStream.close();
      }
      if (resultInputStream != null) {
        resultInputStream.close();
      }
    }
  }


  /**
   * @param destinationFolder the destinationFolder to set
   */
  public void setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
  }


  /**
   * @param cssDestinationFolder the cssDestinationFolder to set
   */
  public void setCssDestinationFolder(final File cssDestinationFolder) {
    this.cssDestinationFolder = cssDestinationFolder;
  }


  /**
   * @param jsDestinationFolder the jsDestinationFolder to set
   */
  public void setJsDestinationFolder(final File jsDestinationFolder) {
    this.jsDestinationFolder = jsDestinationFolder;
  }

  /**
   * @param versionEncoder(wroManagerFactory) the wroManagerFactory to set
   */
  public void setWroManagerFactory(final String wroManagerFactory) {
    this.wroManagerFactory = wroManagerFactory;
  }
}
