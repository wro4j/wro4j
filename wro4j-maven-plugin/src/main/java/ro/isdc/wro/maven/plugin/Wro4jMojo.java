/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
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


/**
 * @goal bundle
 * @phase package
 */
public class Wro4jMojo extends AbstractMojo {
//  private static final Logger LOG = LoggerFactory.getLogger(Wro4jMojo.class);
  /**
   * File containing the groups definitions.
   * @parameter
   */
  private File wroFile;
  /**
   * @parameter
   * @optional
   */
  private List<String> targetGroups = new ArrayList<String>();
  /**
   * The path to the destination directory where the files are stored at the end of the process.
   *
   * @parameter default-value="${project.build.directory}/wro"
   */
  private String destinationFolder;
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
    getLog().debug("executing the mojo");
    getLog().debug("wro file: " + wroFile);
    try {
    	//TODO create a Request object
      for (final String group : targetGroups) {
        processGroup(group);
      }
    } catch(final Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  /**
   * @throws IOException
   */
  private void processGroup(final String group)
    throws IOException {
    getLog().info("processing group: " + group);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(request.getRequestURI()).thenReturn(group);
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(System.out));
    getFactory().getInstance().process(request, response);
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
   * @return the destinationFolder
   */
  public String getDestinationFolder() {
    return this.destinationFolder;
  }

  /**
   * @param destinationFolder the destinationFolder to set
   */
  public void setDestinationFolder(final String destinationFolder) {
    this.destinationFolder = destinationFolder;
  }
}
