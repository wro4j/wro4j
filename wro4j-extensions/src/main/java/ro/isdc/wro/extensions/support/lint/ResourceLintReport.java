package ro.isdc.wro.extensions.support.lint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;


/**
 * Encapsulates lint reports of type <T> found for a resource. This class is generic, because there are different
 * types of errors.
 *
 * @author Alex Objelean
 * @created 15 Sep 2012
 * @since 1.5.0
 */
public class ResourceLintReport<T> {
  /**
   * @VisibleForTesting
   */
  static final String UNKNOWN_PATH = "UnknownFile";
  private Collection<T> lints = new ArrayList<T>();
  private String resourcePath = UNKNOWN_PATH;

  /**
   * Required for JSON serialization.
   */
  public ResourceLintReport() {
  }

  /**
   * Factory method for creating a {@link ResourceLintReport} instance.
   *
   * @param resourcePath
   *          the path of the of the resource
   * @param lints
   *          a collection of lints associated with a resource.
   * @return {@link ResourceLintReport} with all lints set.
   */
  public static <T> ResourceLintReport<T> create(final String resourcePath, final Collection<T> lints) {
    return new ResourceLintReport<T>(resourcePath, lints);
  }

  private ResourceLintReport(final String resourcePath, final Collection<T> errors) {
    this.resourcePath = StringUtils.isEmpty(resourcePath) ? UNKNOWN_PATH : resourcePath;
    this.lints = errors == null ? Collections.<T> emptyList() : errors;
  }

  /**
   * @return a readonly collection of errors containd in this resource.
   */
  public Collection<T> getLints() {
    return Collections.unmodifiableCollection(lints);
  }

  /**
   * @return the path to the resource containing errors.
   */
  public String getResourcePath() {
    return resourcePath;
  }

  public void setLints(final Collection<T> lints) {
    Validate.notNull(lints);
    this.lints = lints;
  }

  public void setResourcePath(final String resourcePath) {
    this.resourcePath = resourcePath == null ? UNKNOWN_PATH : resourcePath;
  }
}
