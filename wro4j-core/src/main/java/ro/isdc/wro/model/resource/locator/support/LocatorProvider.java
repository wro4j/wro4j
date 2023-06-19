package ro.isdc.wro.model.resource.locator.support;

import java.util.Map;

import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * All implementation of this interface will contribute to the list of available locators discovered during
 * application initialization.
 * 
 * @author Alex Objelean
 */
public interface LocatorProvider {
  /**
   * @return the locators to contribute. The key represents the locator alias.
   */
  Map<String, UriLocator> provideLocators();
}
