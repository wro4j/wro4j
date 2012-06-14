/*
 * Copyright (C) 2011 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.AbstractResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;


/**
 * Default implementation of {@link ResourceLocatorFactory}. This implementation relies on wro4j {@link Context} object
 * and cannot be used without it. The algorithm of returning best suited {@link ResourceLocator} is based on uri
 * analysis. If the uri starts with a prefix some {@link ResourceLocator} can handle, it will be used. Eventually, the
 * {@link UrlResourceLocator} is used if no other best suited locator is found.
 *
 * @author Alex Objelean
 * @created 31 Mar 2011
 * @since 1.4.0
 */
public abstract class DefaultResourceLocatorFactory
    implements ResourceLocatorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceLocatorFactory.class);

  /**
   * Prevent instantiation. Use factory methods.
   */
  private DefaultResourceLocatorFactory() {
  }

  /**
   * {@inheritDoc}
   */
  public ResourceLocator locate(final String uri) {
    Validate.notNull(uri);
    if (uri.startsWith(ClasspathResourceLocator.PREFIX)) {
      return new ClasspathResourceLocator(uri);
    }
    if (uri.startsWith(ServletContextResourceLocator.PREFIX)) {
      return newServletContextResourceLocator(uri);
    }
    return new UrlResourceLocator(uri);
  }

  /**
   * @return {@link ResourceLocator} handling servletContext resources (starting with /).
   */
  protected abstract ResourceLocator newServletContextResourceLocator(final String uri);


  public static ResourceLocatorFactory standaloneFactory(final File contextFolder) {
    return new DefaultResourceLocatorFactory() {
      @Override
      protected ResourceLocator newServletContextResourceLocator(final String uri) {
        return new AbstractResourceLocator() {
          public InputStream getInputStream()
            throws IOException {
            if (getWildcardStreamLocator().hasWildcard(uri)) {
              final String fullPath = FilenameUtils.getFullPath(uri);
              final String realPath = contextFolder.getPath() + fullPath;
              return getWildcardStreamLocator().locateStream(uri, new File(realPath));
            }

            LOG.debug("locating uri: " + uri);
            final String uriWithoutPrefix = uri.replaceFirst(ServletContextResourceLocator.PREFIX, "");
            final File file = new File(contextFolder, uriWithoutPrefix);
            LOG.debug("Opening file: " + file.getPath());
            return new FileInputStream(file);
          }
        };
      }
    };
  }

  public static ResourceLocatorFactory contextAwareFactory() {
    return new DefaultResourceLocatorFactory() {
      @Override
      protected ResourceLocator newServletContextResourceLocator(final String uri) {
        return new ServletContextResourceLocator(Context.get().getServletContext(), uri);
      }
    };
  }
}
