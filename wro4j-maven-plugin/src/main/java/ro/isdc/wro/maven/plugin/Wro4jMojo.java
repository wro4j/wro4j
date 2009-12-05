/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.impl.StandAloneWroManagerFactory;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;


/**
 * @goal create
 * @phase package
 */
public class Wro4jMojo extends AbstractMojo {
  private static final Logger LOG = LoggerFactory.getLogger(Wro4jMojo.class);
  /**
   * File containing the groups definitions.
   * @parameter
   * @required
   */
  private File wroFile;
  /**
   * @parameter
   * @optional
   */
  private List<String> targetGroups;
  private File cssOut;
  private File jsOut;
//   * @parameter expression="${project.build.directory}"
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
    LOG.debug("executing the mojo");
    LOG.debug("wro file: " + wroFile);
    try {
      getFactory().getInstance().process("test.js");
    } catch(final WroRuntimeException e) {
      throw new MojoExecutionException(e.getMessage(), e);
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

}
