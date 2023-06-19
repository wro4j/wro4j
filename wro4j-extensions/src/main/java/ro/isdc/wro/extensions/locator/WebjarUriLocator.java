package ro.isdc.wro.extensions.locator;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;

/**
 * <p>Locator responsible for locating webjar resources. A webjar resource is a classpath resource respecting a certain
 * standard. <a href="http://www.webjars.org/">Read more</a> about webjars.</p>
 *
 * <p>This locator uses the following prefix to identify a locator capable of handling webjar resources:</p>
 * <code>webjar:</code>
 *
 * @author Alex Objelean
 * @since 1.6.2
 */
public class WebjarUriLocator implements UriLocator {

  private static final Logger LOG = LoggerFactory.getLogger(WebjarUriLocator.class);

  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "webjar";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = ALIAS + ":";

  private final UriLocator classpathLocator = new ClasspathUriLocator();

  /**
   * An instance of {@link WebJarAssetLocator} to be used for identifying the fully qualified name of resources
   *         based on provided partial path.
   */
  private final WebJarAssetLocator webjarAssetLocator = new WebJarAssetLocator();

  /**
   * @return the uri which is acceptable by this locator.
   */
  public static String createUri(final String path) {
    notNull(path);
    return PREFIX + path;
  }

  @Override
  public InputStream locate(final String uri)
      throws IOException {
    LOG.debug("Locating: {}", uri);
    try {
      final String fullpath = webjarAssetLocator.getFullPath(extractPath(uri));
      return classpathLocator.locate(ClasspathUriLocator.createUri(fullpath));
    } catch (final Exception e) {
      throw new IOException("No webjar with uri: " + uri + " available.", e);
    }
  }

  /**
   * Replaces the protocol specific prefix and removes the query path if it exist, since it should not be accepted.
   */
  private String extractPath(final String uri) {

	  String uriWithUpdatedPrefix = uri.trim().replace(PREFIX, StringUtils.EMPTY);

	  return DefaultWildcardStreamLocator.stripQueryPath(uriWithUpdatedPrefix);
  }

  @Override
  public boolean accept(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }
}
