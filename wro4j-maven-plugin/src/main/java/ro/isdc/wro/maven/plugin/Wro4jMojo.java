/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mockito.Mockito;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.maven.plugin.support.AggregatedFolderPathResolver;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;


/**
 * A build-time solution for organizing and minimizing static resources. By default uses the same configuration as the
 * run-time solution. Additionally, allows you to change the processors used by changing the wroManagerFactory
 * implementation used by the plugin.
 *
 * @author Alex Objelean
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class Wro4jMojo
    extends AbstractWro4jMojo {
  /**
   * The path to the destination directory where the files are stored at the end of the process.
   *
   * @parameter default-value="${project.build.directory}" property="destinationFolder"
   * @optional
   */
  private File destinationFolder;
  /**
   * @parameter property="cssDestinationFolder"
   * @optional
   */
  private File cssDestinationFolder;
  /**
   * @parameter property="jsDestinationFolder"
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
   * @parameter property="groupNameMappingFile"
   * @optional
   */
  private File groupNameMappingFile;
  /**
   * Useful when the application is deployed under a contextPath which is different than ROOT (example: "myapp"). This
   * will be used by CssUrlRewritingProcessor to compute properly the url's. By default, the ROOT is assumed, meaning
   * that the rewritten url's will start with "/".
   *
   * @parameter property="contextPath"
   * @optional
   */
  private String contextPath;
  /**
   * Holds a mapping between original group name file & renamed one.
   */
  private final Properties groupNames = new Properties();

  @Override
  protected void validate()
      throws MojoExecutionException {
    super.validate();
    // additional validation requirements
    if (destinationFolder == null) {
      throw new MojoExecutionException("destinationFolder was not set!");
    }
  }

  @Override
  protected void onBeforeExecute() {
    groupNames.clear();
    if (groupNameMappingFile != null && isIncrementalBuild()) {
      try {
        // reuse stored properties for incremental build
        groupNames.load(new AutoCloseInputStream(new FileInputStream(groupNameMappingFile)));
      } catch (final IOException e) {
        getLog().debug("Cannot load " + groupNameMappingFile.getPath());
      }
    }
  }

  @Override
  protected void doExecute()
      throws Exception {
    if (contextPath != null) {
      getLog().info("contextPath: " + contextPath);
    }
    getLog().info("destinationFolder: " + destinationFolder);
    if (jsDestinationFolder != null) {
      getLog().info("jsDestinationFolder: " + jsDestinationFolder);
    }
    if (cssDestinationFolder != null) {
      getLog().info("cssDestinationFolder: " + cssDestinationFolder);
    }
    if (groupNameMappingFile != null) {
      getLog().info("groupNameMappingFile: " + groupNameMappingFile);
    }
    final Collection<String> groupsAsList = getTargetGroupsAsList();
    final StopWatch watch = new StopWatch();
    watch.start("processGroups: " + groupsAsList);

    final Collection<Callable<Void>> callables = new ArrayList<>();

    for (final String group : groupsAsList) {
      for (final ResourceType resourceType : ResourceType.values()) {
        final File destinationFolder = computeDestinationFolder(resourceType);
        final String groupWithExtension = group + "." + resourceType.name().toLowerCase();

        if (isParallelProcessing()) {
          callables.add(Context.decorate(new Callable<Void>() {
            public Void call()
                throws Exception {
              processGroup(groupWithExtension, destinationFolder);
              return null;
            }
          }));
        } else {
          processGroup(groupWithExtension, destinationFolder);
        }
      }
    }
    if (isParallelProcessing()) {
      getTaskExecutor().submit(callables);
    }
    watch.stop();
    getLog().debug(watch.prettyPrint());
    writeGroupNameMap();
  }

  @Override
  protected boolean isIncrementalCheckRequired() {
    return super.isIncrementalCheckRequired() && destinationFolder.exists();
  }

  private void writeGroupNameMap()
      throws Exception {
    if (groupNameMappingFile != null) {

      final File mappingFileParent = new File(groupNameMappingFile.getParent());
      // create missing folders if needed
      mappingFileParent.mkdirs();

      try (FileOutputStream outputStream = new FileOutputStream(groupNameMappingFile)) {
        groupNames.store(outputStream, "Mapping of defined group name to renamed group name");
      } catch (final FileNotFoundException ex) {
        throw new MojoExecutionException("Unable to save group name mapping file", ex);
      }
    }
  }

  /**
   * Encodes a version using some logic.
   *
   * @param group
   *          the name of the resource to encode.
   * @param input
   *          the stream of the result content.
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
   * @param resourceType
   *          {@link ResourceType} to process.
   * @return destinationFoder where the result of resourceType will be copied.
   * @throws MojoExecutionException
   *           if computed folder is null.
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
      Mockito.when(request.getContextPath()).thenReturn(normalizeContextPath(contextPath));
      Mockito.when(request.getRequestURI()).thenReturn(group);
      // mock response
      final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
      resultOutputStream = new ByteArrayOutputStream();
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(resultOutputStream));

      // init context
      final WroConfiguration config = Context.get().getConfig();
      // the maven plugin should ignore empty groups, since it will try to process all types of resources.
      config.setIgnoreEmptyGroup(true);
      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);

      Context.get().setAggregatedFolderPath(getAggregatedPathResolver().resolve());
      // perform processing
      getManagerFactory().create().process();
      // encode version & write result to file
      resultInputStream = new UnclosableBufferedInputStream(resultOutputStream.toByteArray());
      final File destinationFile = new File(parentFoder, rename(group, resultInputStream));
      final File parentFolder = destinationFile.getParentFile();
      if (!parentFolder.exists()) {
        // make directories if required
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
        getLog().info(destinationFile.getAbsolutePath() + " (" + destinationFile.length() + " bytes" + ")");
      }
    } finally {
      // instruct the build about the change in context of incremental build
      if (getBuildContext() != null) {
        getBuildContext().refresh(parentFoder);
      }
      if (resultOutputStream != null) {
        resultOutputStream.close();
      }
      if (resultInputStream != null) {
        resultInputStream.close();
      }
    }
  }

  /**
   * @return normalized representation of the context path. Example: "/myapp". Will add or strip "/" separator depending
   *         on provided input.
   */
  private String normalizeContextPath(final String contextPath) {
    final String separator = ServletContextUriLocator.PREFIX;
    final StringBuffer sb = new StringBuffer(separator);
    if (contextPath != null) {
      String normalizedContextPath = contextPath;
      normalizedContextPath = StringUtils.removeStart(normalizedContextPath, separator);
      normalizedContextPath = StringUtils.removeEnd(normalizedContextPath, separator);
      sb.append(normalizedContextPath);
    }
    return sb.toString();
  }

  private AggregatedFolderPathResolver getAggregatedPathResolver() {
    return new AggregatedFolderPathResolver().setBuildDirectory(buildDirectory).setBuildFinalName(buildFinalName).setContextFoldersAsCSV(
        getContextFoldersAsCSV()).setCssDestinationFolder(cssDestinationFolder).setDestinationFolder(destinationFolder).setLog(
        getLog());
  }

  /**
   * @param destinationFolder
   *          the destinationFolder to set
   */
  void setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
  }

  /**
   * @param cssDestinationFolder
   *          the cssDestinationFolder to set
   */
  void setCssDestinationFolder(final File cssDestinationFolder) {
    this.cssDestinationFolder = cssDestinationFolder;
  }

  /**
   * @param jsDestinationFolder
   *          the jsDestinationFolder to set
   */
  void setJsDestinationFolder(final File jsDestinationFolder) {
    this.jsDestinationFolder = jsDestinationFolder;
  }

  /**
   * The folder where the project is built.
   *
   * @param buildDirectory
   *          the buildDirectory to set
   */
  void setBuildDirectory(final File buildDirectory) {
    this.buildDirectory = buildDirectory;
  }

  /**
   * @param buildFinalName
   *          the buildFinalName to set
   */
  public void setBuildFinalName(final File buildFinalName) {
    this.buildFinalName = buildFinalName;
  }

  /**
   * @param groupNameMappingFile
   *          the groupNameMappingFile to set
   */
  void setGroupNameMappingFile(final File groupNameMappingFile) {
    this.groupNameMappingFile = groupNameMappingFile;
  }

  void setContextPath(final String contextPath) {
    this.contextPath = contextPath;
  }
}
