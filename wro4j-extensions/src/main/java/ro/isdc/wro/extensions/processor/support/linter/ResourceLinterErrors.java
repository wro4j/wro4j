package ro.isdc.wro.extensions.processor.support.linter;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;


/**
 * Encapsulates lint errors of type <T> found inside a resource. This class is generic, because there are different
 * types of errors.
 * 
 * @author Alex Objelean
 * @created 15 Sep 2012
 * @since 1.4.10
 */
public class ResourceLinterErrors<T> {
  private static final String UNKNOWN_PATH = "UnknownFile";
  private Collection<T> errors;
  private String resourcePath;
  
  /**
   * Factory method for creating a {@link ResourceLinterErrors} instance.
   * 
   * @param resourcePath
   *          the path of the of the resource
   * @param errors
   * @return
   */
  public static <T> ResourceLinterErrors<T> create(final String resourcePath, final Collection<T> errors) {
    return new ResourceLinterErrors<T>(resourcePath, errors);
  }
  
  private ResourceLinterErrors(final String resourcePath, final Collection<T> errors) {
    this.resourcePath = StringUtils.isEmpty(resourcePath) ? UNKNOWN_PATH : resourcePath;
    this.errors = errors == null ? Collections.<T> emptyList() : errors;
  }
  
  /**
   * @return a readonly collection of errors containd in this resource.
   */
  public Collection<T> getErrors() {
    return Collections.unmodifiableCollection(errors);
  }
  
  /**
   * @return the path to the resource containing errors.
   */
  public String getResourcePath() {
    return resourcePath;
  }
}
