package ro.isdc.wro.model.resource.locator.custom;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.util.StringUtils;

public class CustomClasspathLocatorProvider implements LocatorProvider {

  @Override
  public Map<String, UriLocator> provideLocators() {
    UriLocator locator = new CustomClasspathUriLocator();
    return singletonMap(CustomClasspathUriLocator.ALIAS, locator);
  }

  private static class CustomClasspathUriLocator extends ClasspathUriLocator {

    public static final String ALIAS = "custom";
    public static final String PREFIX = format("%s:", ALIAS);

    public boolean accept(final String url) {
      return url.trim().startsWith(PREFIX);
    }

    public InputStream locate(final String uri) throws IOException {
      Validate.notNull(uri, "URI cannot be NULL!");
      String location = StringUtils.cleanPath(uri.replaceFirst(PREFIX, "")).trim();

      final InputStream is = getClass().getResource(location).openStream();
      if (is == null) {
        throw new IOException("Couldn't get InputStream from this resource: " + uri);
      }
      return is;
    }
  }
}
