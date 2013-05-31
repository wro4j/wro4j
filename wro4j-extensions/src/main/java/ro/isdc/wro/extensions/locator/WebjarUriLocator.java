package ro.isdc.wro.extensions.locator;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;

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
public class WebjarUriLocator
    implements UriLocator {
  private static final Logger LOG = LoggerFactory.getLogger(WebjarUriLocator.class);
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "webjar";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = format("%s:", ALIAS);
  private final UriLocator classpathLocator = new ClasspathUriLocator();
  private final WebJarAssetLocator webjarAssetLocator = newWebJarAssetLocator();


  /**
   * @return an instance of {@link WebJarAssetLocator} to be used for identifying the fully qualified name of resources
   *         based on provided partial path.
   */
  private WebJarAssetLocator newWebJarAssetLocator() {
    return new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(
        Pattern.compile(".*"), Thread.currentThread().getContextClassLoader()));
  }

  /**
   * @return the uri which is acceptable by this locator.
   */
  public static String createUri(final String path) {
    notNull(path);
    return PREFIX + path;
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream locate(final String uri)
      throws IOException {
    LOG.debug("locating webjar: {}", uri);
    try {
      final String fullpath = webjarAssetLocator.getFullPath(extractPath(uri));
      return classpathLocator.locate(ClasspathUriLocator.createUri(fullpath));
    } catch (final Exception e) {
      throw new IOException("No webjar with uri: " + uri + " available.", e);
    }
  }


  private String extractPath(final String uri) {
    return uri.replace(PREFIX, "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }
}
