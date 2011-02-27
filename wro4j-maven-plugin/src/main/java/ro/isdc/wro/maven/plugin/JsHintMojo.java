/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal jshint
 * @phase process-resources
 * @requiresDependencyResolution runtime
 *
 * @author Alex Objelean
 */
public class JsHintMojo extends AbstractMojo {
  /**
   * {@inheritDoc}
   */
  public void execute()
    throws MojoExecutionException, MojoFailureException {

  }
}
