/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.mockito.Mockito;

import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.StandAloneWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.JawrCssMinifierProcessor;


/**
 * @goal run
 * @phase package
 *
 * @author Alex Objelean
 */
public class Wro4jMojo extends AbstractMojo {
  /**
   * File containing the groups definitions.
   *
   * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/wro.xml
   */
  private File wroFile;
  /**
   * The folder where web application context resides useful for locating resources relative to servletContext .
   *
   * @parameter default-value="${basedir}/src/main/webapp/
   */
  private File contextFolder;
  /**
   * The path to the destination directory where the files are stored at the end of the process.
   *
   * @parameter default-value="${project.build.directory}/wro/
   */
  private File destinationFolder;
  /**
   * @parameter
   * @optional
   */
  private List<String> targetGroups = new ArrayList<String>();
  /**
   * @parameter default-value="true"
   * @optional
   */
  private boolean minimize;
  /**
   * Factory which will create the engine for doing the main job.
   */
  private WroManagerFactory wroManagerFactory;

  /**
   * @return {@link WroManagerFactory} implementation.
   */
  private WroManagerFactory getFactory() {
    if (wroManagerFactory == null) {
      wroManagerFactory = new StandAloneWroManagerFactory() {
        @Override
        protected WroModelFactory newModelFactory() {
          return new XmlModelFactory() {
            @Override
            protected InputStream getConfigResourceAsStream() throws IOException {
              return new FileInputStream(wroFile);
            }
          };
        }
        @Override
        protected GroupsProcessor newGroupsProcessor() {
          final GroupsProcessor groupsProcessor = super.newGroupsProcessor();
          groupsProcessor.addPreProcessor(new BomStripperPreProcessor());
          groupsProcessor.addPostProcessor(new CssVariablesProcessor());
          if (minimize) {
            groupsProcessor.addPostProcessor(new JSMinProcessor());
            groupsProcessor.addPostProcessor(new JawrCssMinifierProcessor());
          }
          return groupsProcessor;
        }
        @Override
        protected ServletContextUriLocator newServletContextUriLocator() {
          return new ServletContextUriLocator() {
            @Override
            public InputStream locate(final String uri)
              throws IOException {
              final String uriWithoutPrefix = uri.replaceFirst(PREFIX, "");
              return new FileInputStream(new File(contextFolder, uriWithoutPrefix));
            }
          };
        }
      };
    }
    return wroManagerFactory;
  }

  /**
   * {@inheritDoc}
   */
  public void execute()
    throws MojoExecutionException {
    getLog().info("Executing the mojo");
    getLog().debug("wro file: " + wroFile);
    try {
      if (!destinationFolder.exists()) {
        destinationFolder.mkdirs();
      }
      getLog().info("will process the following groups: " + targetGroups);
    	//TODO create a Request object
      for (final String group : targetGroups) {
        for (final ResourceType resourceType : ResourceType.values()) {
          final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
          processGroup(groupWithExtension);
        }
      }
    } catch(final Exception e) {
      throw new MojoExecutionException("Exception occured while processing: " + e.getMessage(), e);
    }
  }

  /**
   * Process a single group.
   * @throws IOException if any IO related exception occurs.
   */
  private void processGroup(final String group)
    throws IOException {
    getLog().info("processing group: " + group);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(request.getRequestURI()).thenReturn(group);
    final File destinationFile = new File(destinationFolder, group);
    destinationFile.createNewFile();
    final FileOutputStream fos = new FileOutputStream(destinationFile);
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(fos));
    getFactory().getInstance().process(request, response);
    fos.close();
    //delete empty files
    if (destinationFile.length() == 0) {
      getLog().info("No content found for group: " + group);
      destinationFile.delete();
    } else {
      getLog().info(destinationFile.getAbsolutePath() + " (" + destinationFile.length() + "bytes"
        + ") has been created!");
    }
  }

  /**
   * @param wroFile the wroFile to set
   */
  public void setWroFile(final File wroFile) {
    this.wroFile = wroFile;
  }

  /**
   * @param targetGroups the targetGroups to set
   */
  public void setTargetGroups(final List<String> targetGroups) {
    this.targetGroups = targetGroups;
  }

  /**
   * @param destinationFolder the destinationFolder to set
   */
  public void setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
  }

  /**
   * @param contextFolder the servletContextFolder to set
   */
  public void setContextFolder(final File contextFolder) {
    this.contextFolder = contextFolder;
  }
}
