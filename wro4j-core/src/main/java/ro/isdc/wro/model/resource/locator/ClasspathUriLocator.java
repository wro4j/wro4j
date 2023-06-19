/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.JarWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer.NoMoreAttemptsIOException;
import ro.isdc.wro.util.StringUtils;


/**
 * Implementation of the {@link UriLocator} that is able to read a resource from a classpath.
 *
 * @author Alex Objelean
 */
public class ClasspathUriLocator
    extends WildcardUriLocatorSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathUriLocator.class);
  /**
   * Alias used to register this locator with {@link LocatorProvider}.
   */
  public static final String ALIAS = "classpath";
  /**
   * Prefix of the resource uri used to check if the resource can be read by this {@link UriLocator} implementation.
   */
  public static final String PREFIX = format("%s:", ALIAS);

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
  public boolean accept(final String url) {
    return isValid(url);
  }

  /**
   * Check if a uri is a classpath resource.
   *
   * @param uri
   *          to check.
   * @return true if the uri is a classpath resource.
   */
  public static boolean isValid(final String uri) {
    return uri.trim().startsWith(PREFIX);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    Validate.notNull(uri, "URI cannot be NULL!");
    // replace prefix & clean path by removing '..' characters if exists and
    // normalizing the location to use.
    String location = StringUtils.cleanPath(uri.replaceFirst(PREFIX, "")).trim();

    if (getWildcardStreamLocator().hasWildcard(location)) {
      try {
        return locateWildcardStream(uri, location);
      } catch (final IOException e) {
        if (location.contains("?")) {
          location = DefaultWildcardStreamLocator.stripQueryPath(location);
          LOG.debug("Trying fallback location: {}", location);
        }
      }
    }
    final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    if (is == null) {
      throw new IOException("Couldn't get InputStream from this resource: " + uri);
    }
    return is;
  }

  /**
   * @return an input stream for an uri containing a wildcard for a given location.
   */
  private InputStream locateWildcardStream(final String uri, final String location)
      throws IOException {
    LOG.debug("wildcard detected for location: {}", location);
    // prefix with '/' because we use class relative resource retrieval. Using ClassLoader.getSystemResource doesn't
    // work well.
    final String fullPath = "/" + FilenameUtils.getFullPathNoEndSeparator(location);
    URL url = getClass().getResource(fullPath);
    LOG.debug("Attempting to find resource {} at the following location: {}", uri, fullPath);
    try {
      return locateWildcardStream(uri, url);
    } catch (final IOException e) {
      //do not attempt unless exception is of this type
      if (e instanceof NoMoreAttemptsIOException) {
        throw e;
      }
      // try once more, in order to treat classpath resources located in the currently built project.
      url = getClass().getResource("");
      LOG.debug("Attempting to find resource {} at the following URL: {}", uri, url);
      return locateWildcardStream(uri, url);
    }
  }

  private InputStream locateWildcardStream(final String uri, final URL url)
      throws IOException {
    if (url == null) {
      LOG.debug("Failed to locate stream for {} because URL is null", uri);
      throw new IOException("Cannot locate stream for null URL");
    }
    return getWildcardStreamLocator().locateStream(uri, new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name())));
  }

  /**
   * Builds a {@link JarWildcardStreamLocator} in order to get resources from the full classpath.
   */
  @Override
  public WildcardStreamLocator newWildcardStreamLocator() {
    return new JarWildcardStreamLocator() {
      @Override
      public boolean hasWildcard(final String uri) {
        return isEnableWildcards() && super.hasWildcard(uri);
      }
    };
  }
}
