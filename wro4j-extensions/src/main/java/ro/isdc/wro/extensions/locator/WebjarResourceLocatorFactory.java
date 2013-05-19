package ro.isdc.wro.extensions.locator;

import java.util.regex.Pattern;

import org.webjars.WebJarAssetLocator;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.AbstractResourceLocatorFactory;


/**
 * A factory responsible for returning {@link WebjarResourceLocator}/
 *
 * @author Alex Objelean
 */
public class WebjarResourceLocatorFactory
    extends AbstractResourceLocatorFactory {
  private final WebJarAssetLocator webJarAssetLocator = newWebJarAssetLocator();

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceLocator getLocator(final String uri) {
    return uri.startsWith(WebjarResourceLocator.PREFIX) ? new WebjarResourceLocator(uri, webJarAssetLocator) : null;
  }

  /**
   * @return an instance of {@link WebJarAssetLocator} to be used for identifying the fully qualified name of resources
   *         based on provided partial path.
   */
  private WebJarAssetLocator newWebJarAssetLocator() {
    return new WebJarAssetLocator(WebJarAssetLocator.getFullPathIndex(Pattern.compile(".*"), Thread.currentThread()
        .getContextClassLoader()));
  }
}
