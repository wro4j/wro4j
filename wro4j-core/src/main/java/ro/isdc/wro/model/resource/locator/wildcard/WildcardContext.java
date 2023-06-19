package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;

import org.apache.commons.io.FilenameUtils;


/**
 * Defines a wildcard context based on provided resource uri and the folder representing the starting point of the
 * search for the resources.
 *
 * @author Alex Objelean
 * @since 1.4.4
 */
public final class WildcardContext {
  private String uri;
  private File folder;

  /**
   * @param uri the resource uri containing the wildcard.
   * @param folder the folder representing the parent folder of the uri where the search will be performed.
   */
  public WildcardContext(final String uri, final File folder) {
    this.uri = uri;
    this.folder = folder;
  }
  public String getUri() {
    return this.uri;
  }
  public File getFolder() {
    return this.folder;
  }
  /**
   * @return the part of the uri containing the wildcard.
   */
  public String getWildcard() {
    return FilenameUtils.getName(uri);
  }
}