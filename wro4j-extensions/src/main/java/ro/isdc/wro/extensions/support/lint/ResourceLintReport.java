package ro.isdc.wro.extensions.support.lint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;


/**
 * Encapsulates lint reports of type {@code <T>} found for a resource. This class is generic, because there are different
 * types of errors.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class ResourceLintReport<T> {

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
    this.lints = filter(errors);
  }

  /**
   * This filtering is required, in order to ensure that no nulls are passed (which happens when using gson for
   * deserializing json collection.
   *
   * @param collection
   *          to filter.
   * @return a null free collection of item by filtering found nulls.
   */
  private List<T> filter(final Collection<T> collection) {
    final List<T> nullFreeList = new ArrayList<T>();
    if (collection != null) {
      for (final T item : collection) {
        if (item != null) {
          nullFreeList.add(item);
        }
      }
    }
    return nullFreeList;
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
