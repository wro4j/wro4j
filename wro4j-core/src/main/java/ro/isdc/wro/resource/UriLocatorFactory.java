/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.resource;

/**
 * A factory responsible for creating a ResourceLocator based on provided uri.
 * If factory is unable to create a resource, it will throw a runtime exception.
 *
 * @author Alex Objelean
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

  /**
   * Add a single resource to the list of supported resource locators.
   *
   * @param uriLocator
   *          {@link UriLocator} object to add.
   */
  void addUriLocator(final UriLocator uriLocator);
}
