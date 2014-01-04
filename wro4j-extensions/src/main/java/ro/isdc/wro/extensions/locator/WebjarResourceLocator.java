package ro.isdc.wro.extensions.locator;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;

/**
 * Locator responsible for locating webjar resources. A webjar resource is a classpath resource respecting a certain
 * standard. <a href="http://www.webjars.org/">Read more</a> about webjars.
 * <p/>
 * This locator uses the following prefix to identify a locator capable of handling webjar resources:
 * <code>webjar:</code>
 *
 * @author Alex Objelean
 * @created 6 Jan 2013
 * @since 1.6.2
 */
public class WebjarResourceLocator
    implements ResourceLocator {
  private static final Logger LOG = LoggerFactory.getLogger(WebjarResourceLocator.class);
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "webjar";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = format("%s:", ALIAS);
  private final WebJarAssetLocator webjarAssetLocator;


  /**
   * @return the uri which is acceptable by this locator.
   */
  public static String createUri(final String path) {
    notNull(path);
    return PREFIX + path;
  }

  /**
   * Classpath location. This value is expected to be prefixed with "webjar:" value.
   */
  private final String path;

  public WebjarResourceLocator(final String path, final WebJarAssetLocator webjarAssetLocator) {
    notNull(path);
    this.path = path;
    this.webjarAssetLocator = webjarAssetLocator;
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator createRelative(final String relativePath)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long lastModified() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream getInputStream()
      throws IOException {
    LOG.debug("locating webjar: {}", path);
    try {
      final String fullpath = webjarAssetLocator.getFullPath(extractPath(path));
      return new ClasspathResourceLocator(ClasspathResourceLocator.createUri(fullpath)).getInputStream();
    } catch (final Exception e) {
      throw new IOException("No webjar with uri: " + path + " available.", e);
    }
  }

  /**
   * Replaces the protocol specific prefix and removes the query path if it exist, since it should not be accepted.
   */
  private String extractPath(final String uri) {
    return DefaultWildcardStreamLocator.stripQueryPath(uri.replace(PREFIX, ""));
  }
}
