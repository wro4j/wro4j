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
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;


/**
 * @goal bundle
 * @phase package
 */
public class Wro4jMojo extends AbstractMojo {
  /**
   * File containing the groups definitions.
   * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/wro.xml
   */
  private File wroFile;
  /**
   * File containing the groups definitions.
   * @parameter default-value="${basedir}/src/main/webapp/
   */
  private File servletContextFolder;
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
   * Factory which will create the engine for doing the main job.
   */
  private WroManagerFactory wroManagerFactory;

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
        protected ServletContextUriLocator newServletContextUriLocator() {
          return new ServletContextUriLocator() {
            @Override
            public InputStream locate(final String uri)
              throws IOException {
              final String uriWithoutPrefix = uri.replaceFirst(PREFIX, "");
              return new FileInputStream(new File(servletContextFolder, uriWithoutPrefix));
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
    getLog().debug("Creating output file: " + destinationFile.getAbsolutePath());
    final FileOutputStream fos = new FileOutputStream(destinationFile);
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(fos));
    getFactory().getInstance().process(request, response);
    fos.close();
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
   * @param servletContextFolder the servletContextFolder to set
   */
  public void setServletContextFolder(final File servletContextFolder) {
    this.servletContextFolder = servletContextFolder;
  }
}
