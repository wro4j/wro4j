package ro.isdc.wro.maven.plugin.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;

import org.sonatype.plexus.build.incremental.BuildContext;


/**
 * Encapsulate the details about state persisted across multiple build runs.
 *
 * @author Alex Objelean
 * @created 1 Sep 2013
 * @since 1.7.1
 */
public class BuildContextHolder {
  private final BuildContext buildContext;
  private final File buildDirectory;

  /**
   * @param buildContext
   *          - The context provided by maven. If this is not provided, the default mechanism for persisting build data
   *          is used.
   * @param buildDirectory
   *          the folder where the build output is created.
   */
  public BuildContextHolder(final BuildContext buildContext, final File buildDirectory) {
    notNull(buildDirectory);
    this.buildContext = buildContext;
    this.buildDirectory = buildDirectory;
  }

  /**
   * @param key of the value to retrieve.
   * @return the persisted value stored under the provided key. If no value exist, the returned result will be null.
   */
  public Object getValue(final String key) {
    return null;
  }

  public void setValue(final String key, final Object value) {

  }

}
