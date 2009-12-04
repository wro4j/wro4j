/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import ro.isdc.wro.manager.WroManager;


/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase process-sources
 *
 * @author Alex Objelean
 */
public class WroMojo extends AbstractMojo {
  /**
   * Location of the file.
   *
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File outputDirectory;
  /**
   * Manager, responsible for doing the main job.
   */
  private WroManager wroManager;
  public WroMojo() {
    wroManager = new WroManager();
  }

  /**
   * {@inheritDoc}
   */
  public void execute()
    throws MojoExecutionException {
    final File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }

    final File touch = new File(f, "touch.txt");

    FileWriter w = null;
    try {
      w = new FileWriter(touch);

      w.write("touch.txt");
    } catch (final IOException e) {
      throw new MojoExecutionException("Error creating file " + touch, e);
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (final IOException e) {
          // ignore
        }
      }
    }
  }
}
