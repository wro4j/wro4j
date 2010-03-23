/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Test class for {@link UriLocatorFactory}.
 *
 * @author Alex Objelean
 */
public class TestUriLocatorFactory {
  @Test
  public void testNullUri() {
    final UriLocator uriLocator = new UriLocatorFactoryImpl().getInstance(null);
    Assert.assertNull(uriLocator);
  }

  @Test
  public void testInvalidUri() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    factory.addUriLocator(new ClasspathUriLocator());
    final UriLocator uriLocator = factory.getInstance("http://www.google.com");
    Assert.assertNull(uriLocator);
  }

  @Test
  public void testValidUri() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    factory.addUriLocator(new ClasspathUriLocator());
    Assert.assertNotNull(factory.getInstance("classpath:some/classpath/resource.properties"));
  }
}
