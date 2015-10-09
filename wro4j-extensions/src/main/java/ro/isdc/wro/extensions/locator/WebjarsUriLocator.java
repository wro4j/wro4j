package ro.isdc.wro.extensions.locator;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * Similar to {@link WebjarUriLocator}, but uses "/webjars/" prefix to identify a webjar resource. Useful when resources
 * are located using build-time solution (maven plugin). The only disadvantage of this locator, is that it will not make
 * it possible to use resources located inside a folder named "webjars" (inside the application context).
 *
 * @author Alex Objelean
 * @created 5 Oct 2015
 * @since 1.8.0
 */
public class WebjarsUriLocator
    implements UriLocator {
  private static final Logger LOG = LoggerFactory.getLogger(WebjarsUriLocator.class);
  private static final String PATTERN = ".*";
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "webjars";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  private static final String PREFIX = format("/%s/", ALIAS);
  private final UriLocator classpathLocator = new ClasspathUriLocator();
  private final WebJarAssetLocator webjarAssetLocator = newWebJarAssetLocator();


  /**
   * @return an instance of {@link WebJarAssetLocator} to be used for identifying the fully qualified name of resources
   *         based on provided partial path.
   */
  private WebJarAssetLocator newWebJarAssetLocator() {
    return new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(
        Pattern.compile(PATTERN), Thread.currentThread().getContextClassLoader()));
  }

  /**
   * @return the uri which is acceptable by this locator.
   */
  public static String createUri(final String path) {
    notNull(path);
    return WroUtil.joinPath(PREFIX, path);
  }

  @Override
  public InputStream locate(final String uri)
      throws IOException {
    LOG.debug("locating: {}", uri);
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
    return DefaultWildcardStreamLocator.stripQueryPath(uri.replace(PREFIX, StringUtils.EMPTY));
  }

  @Override
  public boolean accept(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }
}
