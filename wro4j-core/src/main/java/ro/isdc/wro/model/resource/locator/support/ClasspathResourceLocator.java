/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.wildcard.JarWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.util.StringUtils;


/**
 * Locates classpath resources.
 *
 * @author Alex Objelean
 * @created 28 Mar 2011
 * @since 1.4.0
 */
public class ClasspathResourceLocator extends AbstractResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathResourceLocator.class);
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = "classpath:";
  @Inject
  private DuplicateResourceDetector duplicateResourceDetector;
  /**
   * Classpath location. This value is expected to be prefixed with "classpath:" value.
   */
  private final String path;
  /**
   * Same as path but without the prefix.
   */
  private final String location;

  public ClasspathResourceLocator(final String path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null");
    }
    this.path = path;
    // replace prefix & clean path by removing '..' characters if exists and
    // normalizing the location to use.
    location = StringUtils.cleanPath(path.replaceFirst(PREFIX, "")).trim();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long lastModified() {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(location);
    final File file = FileUtils.toFile(url);
    return file != null ? file.lastModified() : super.lastModified();
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getInputStream()
    throws IOException {
    LOG.debug("Reading uri: " + path);

    if (getWildcardStreamLocator().hasWildcard(location)) {
      return locateWildcardStream(path, location);
    }
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    if (is == null) {
      throw new IOException("Couldn't get InputStream from this resource: " + path);
    }
    return is;
  }


  /**
   * @return an input stream for an uri containing a wildcard for a given location.
   */
  private InputStream locateWildcardStream(final String uri, final String location)
    throws IOException {
    LOG.debug("wildcard detected for location: " + location);
    // prefix with '/' because we use class relative resource retrieval. Using ClassLoader.getSystemResource doesn't
    // work well.
    final String fullPath = "/" + FilenameUtils.getFullPathNoEndSeparator(location);
    URL url = getClass().getResource(fullPath);
    if (url == null) {
      // try once more, in order to treat classpath resources located in the currently built project.
      url = getClass().getResource("");
    }
    if (url == null) {
      final String message = "Couldn't get URL for the following path: " + fullPath;
      LOG.warn(message);
      throw new IOException(message);
    }
    return getWildcardStreamLocator().locateStream(uri, new File(url.getFile()));
  }


  /**
   * Builds a {@link JarWildcardStreamLocator} in order to get resources from the full classpath.
   */
  @Override
  protected WildcardStreamLocator newWildcardStreamLocator() {
    return new JarWildcardStreamLocator(duplicateResourceDetector) {
      @Override
      public boolean hasWildcard(final String uri) {
        return !disableWildcards() && super.hasWildcard(uri);
      }
    };
  }


  /**
   * This implementation creates a ClassPathResource, applying the given path
   * relative to the path of the underlying resource of this descriptor.
   * @see org.springframework.util.StringUtils#applyRelativePath(String, String)
   */
  @Override
  public ResourceLocator createRelative(final String relativePath) {
    final String folder = FilenameUtils.getFullPath(location);
    // remove '../' & normalize the path.
    final String pathToUse = StringUtils.normalizePath(folder + relativePath);
    return new ClasspathResourceLocator(PREFIX + pathToUse);
  }
}
