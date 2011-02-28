/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import ro.isdc.wro.manager.factory.standalone.StandaloneContextAwareManagerFactory;



/**
 * Maven plugin used to check the validity of the javascript used in the project.
 *
 * @goal jshint
 * @phase process-resources
 * @requiresDependencyResolution runtime
 *
 * @author Alex Objelean
 */
public class JsHintMojo extends AbstractWro4jMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute()
    throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected StandaloneContextAwareManagerFactory getManagerFactory()
    throws Exception {
    return null;
  }
}
