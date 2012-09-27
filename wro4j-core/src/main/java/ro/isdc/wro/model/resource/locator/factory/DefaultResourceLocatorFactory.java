package ro.isdc.wro.model.resource.locator.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.AbstractResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;


/**
 * Default implementation of {@link ResourceLocatorFactory}. This implementation relies on wro4j {@link Context} object
 * and cannot be used without it. The algorithm of returning best suited {@link ResourceLocator} is based on uri
 * analysis. If the uri starts with a prefix some {@link ResourceLocator} can handle, it will be used. Eventually, the
 * {@link UrlResourceLocator} is used if no other best suited locator is found.
 * 
 * @author Alex Objelean
 * @created 10 Jul 2012
 * @since 2.0
 */
public class DefaultResourceLocatorFactory
    extends SimpleResourceLocatorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceLocatorFactory.class);
  /**
   * Populates the list of factories with default available implementations.
   */
  public DefaultResourceLocatorFactory() {
    addFactory(new ClasspathResourceLocatorFactory()).addFactory(newServletContextLocatorFactory()).addFactory(
        new UrlResourceLocatorFactory());
  }

  /**
   * @return the servletContext locator factory.
   */
  protected ResourceLocatorFactory newServletContextLocatorFactory() {
    return new ServletContextResourceLocatorFactory();
  }
  
  /**
   * @param contextFolder
   * @return a {@link ResourceLocatorFactory} which doesn't require {@link ServletContext}, but uses the contextFolder
   *         to compute servlet context relative location.
   */
  public static ResourceLocatorFactory standaloneFactory(final File contextFolder) {
    return new DefaultResourceLocatorFactory() {
      @Override
      protected ResourceLocatorFactory newServletContextLocatorFactory() {
        return new ServletContextResourceLocatorFactory() {
          @Override
          protected ResourceLocator newLocator(final String uri) {
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
    };
  }
}
