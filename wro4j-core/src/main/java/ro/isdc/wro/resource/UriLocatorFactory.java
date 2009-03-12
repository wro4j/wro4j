/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.resource;

/**
 * A factory responsible for creating a ResourceLocator based on provided uri.
 * If factory is unable to create a resource, it will throw a runtime exception.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public interface UriLocatorFactory {
  /**
   * Returns an instance of {@link UriLocator} based on uri.
   * 
   * @param uri
   *          location of the resource.
   * @return not null {@link UriLocator} implementation.
   * @throws runtime
   *           exception if a valid instance of resourceLocator cannot be
   *           returned.
   */
  UriLocator getInstance(final String uri);
}
