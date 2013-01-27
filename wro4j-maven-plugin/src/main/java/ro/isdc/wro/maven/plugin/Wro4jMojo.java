/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.MojoExecutionException;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;


/**
 * A build-time solution for organizing and minimizing static resources. By default uses the same configuration as the
 * run-time solution. Additionally, allows you to change the processors used by changing the wroManagerFactory
 * implementation used by the plugin.
 *
 * @goal run
 * @phase compile
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
   * This parameter is not meant to be used. The only purpose is to hold project build directory
   *
   * @parameter default-value="${project.build.directory}"
   * @optional
   */
  private File buildDirectory;
  /**
   * This parameter is not meant to be used. The only purpose is to hold the final build name of the artifact
   *
   * @parameter default-value="${project.build.directory}/${project.build.finalName}"
   * @optional
   */
  private File buildFinalName;
  /**
   * @parameter expression="${groupNameMappingFile}"
   * @optional
   */
  private String groupNameMappingFile;
  /**
   * Holds a mapping between original group name file & renamed one.
   */
  private final Properties groupNames = new Properties();

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validate()
    throws MojoExecutionException {
    super.validate();
    // additional validation requirements
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
    getLog().info("groupNameMappingFile: " + groupNameMappingFile);

    final Collection<String> groupsAsList = getTargetGroupsAsList();
    for (final String group : groupsAsList) {
      for (final ResourceType resourceType : ResourceType.values()) {
        final File destinationFolder = computeDestinationFolder(resourceType);
        final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
        processGroup(groupWithExtension, destinationFolder);
      }
    }

    writeGroupNameMap();
  }

  private void writeGroupNameMap()
      throws Exception {
    if (groupNameMappingFile != null) {
      FileOutputStream outputStream = null;
      try {
        outputStream = new FileOutputStream(groupNameMappingFile);
        groupNames.store(outputStream, "Mapping of defined group name to renamed group name");
      } catch (final FileNotFoundException ex) {
        throw new MojoExecutionException("Unable to save group name mapping file", ex);
      } finally {
        IOUtils.closeQuietly(outputStream);
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
      final String newName = getManagerFactory().create().getNamingStrategy().rename(group, input);
      groupNames.setProperty(group, newName);
      return newName;
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

      // mock request
      final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Mockito.when(request.getRequestURI()).thenReturn(group);
      // mock response
      final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
      resultOutputStream = new ByteArrayOutputStream();
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(resultOutputStream));

      // init context
      final WroConfiguration config = Context.get().getConfig();
      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);

      Context.get().setAggregatedFolderPath(computeAggregatedFolderPath());
      // perform processing
      getManagerFactory().create().process();
      // encode version & write result to file
      resultInputStream = new UnclosableBufferedInputStream(resultOutputStream.toByteArray());
      final File destinationFile = new File(parentFoder, rename(group, resultInputStream));
      final File parentFolder = destinationFile.getParentFile();
      if (!parentFolder.exists()) {
        //make directories if required
        parentFolder.mkdirs();
      }
      destinationFile.createNewFile();
      // allow the same stream to be read again
      resultInputStream.reset();
      getLog().debug("Created file: " + destinationFile.getName());

      final OutputStream fos = new FileOutputStream(destinationFile);
      // use reader to detect encoding
      IOUtils.copy(resultInputStream, fos);
      fos.close();
      // delete empty files
      if (destinationFile.length() == 0) {
        getLog().debug("No content found for group: " + group);
        destinationFile.delete();
      } else {
        getLog().info("file size: " + destinationFile.getName() + " -> " + destinationFile.length() + " bytes");
        getLog().info(
          destinationFile.getAbsolutePath() + " (" + destinationFile.length() + " bytes" + ")");
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
   * The idea is to compute the aggregatedFolderPath based on a root folder. The root folder is determined by comparing
   * the cssTargetFolder (the folder where aggregated css files are located) with build directory or contextFolder. If
   * rootFolder is null, then the result is also null (equivalent to using the cssTargetFolder the same as the root
   * folder.
   *
   * @return the aggregated folder path, based on the cssDestinationFolder (if set) and the build folder or the
   *         contextFolder.
   */
  private String computeAggregatedFolderPath() {
    Validate.notNull(buildDirectory, "Build directory cannot be null!");
    String result = null;
    final File cssTargetFolder = cssDestinationFolder == null ? destinationFolder : cssDestinationFolder;
    File rootFolder = null;
    Validate.notNull(cssTargetFolder, "cssTargetFolder cannot be null!");

    if (buildFinalName != null && cssTargetFolder.getPath().startsWith(buildFinalName.getPath())) {
      rootFolder = buildFinalName;
    } else if (cssTargetFolder.getPath().startsWith(buildDirectory.getPath())) {
      rootFolder = buildDirectory;
    } else if (cssTargetFolder.getPath().startsWith(getContextFolder().getPath())) {
      rootFolder = getContextFolder();
    }
    getLog().debug("buildDirectory: " + buildDirectory);
    getLog().debug("contextFolder: " + getContextFolder());
    getLog().debug("cssTargetFolder: " + cssTargetFolder);
    getLog().debug("rootFolder: " + rootFolder);
    if (rootFolder != null) {
      result = StringUtils.removeStart(cssTargetFolder.getPath(), rootFolder.getPath());
    }
    getLog().debug("computedAggregatedFolderPath: " + result);
    return result;
  }


  /**
   * @param destinationFolder the destinationFolder to set
   * @VisibleForTesting
   */
  void setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
  }


  /**
   * @param cssDestinationFolder the cssDestinationFolder to set
   * @VisibleForTesting
   */
  void setCssDestinationFolder(final File cssDestinationFolder) {
    this.cssDestinationFolder = cssDestinationFolder;
  }


  /**
   * @param jsDestinationFolder the jsDestinationFolder to set
   * @VisibleForTesting
   */
  void setJsDestinationFolder(final File jsDestinationFolder) {
    this.jsDestinationFolder = jsDestinationFolder;
  }

  /**
   * The folder where the project is built.
   *
   * @param buildDirectory the buildDirectory to set
   * @VisibleForTesting
   */
  void setBuildDirectory(final File buildDirectory) {
    this.buildDirectory = buildDirectory;
  }

  /**
   * @param groupNameMappingFile the groupNameMappingFile to set
   * @VisibleForTesting
   */
  void setGroupNameMappingFile(final String groupNameMappingFile) {
    this.groupNameMappingFile = groupNameMappingFile;
  }
}
