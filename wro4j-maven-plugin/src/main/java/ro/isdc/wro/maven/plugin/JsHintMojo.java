/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.util.Collection;

import ro.isdc.wro.model.resource.ResourceType;


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
    final Collection<String> groupsAsList = getTargetGroupsAsList();
    for (final String group : groupsAsList) {
      for (final ResourceType resourceType : ResourceType.values()) {
        final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
        processGroup(groupWithExtension);
      }
    }
  }


  /**
   * @param groupWithExtension
   */
  private void processGroup(final String groupWithExtension) {

  }
}
