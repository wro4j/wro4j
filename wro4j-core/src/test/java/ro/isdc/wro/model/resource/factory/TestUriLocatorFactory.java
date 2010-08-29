/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Test class for {@link UriLocatorFactory}.
 *
 * @author Alex Objelean
 */
public class TestUriLocatorFactory {
  private UriLocatorFactoryImpl factory;
  @Before
  public void setUp() {
    final GroupsProcessor groupsProcessor = new GroupsProcessor();
    factory = new UriLocatorFactoryImpl();
    groupsProcessor.setUriLocatorFactory(factory);
  }
  @Test
  public void testNullUri() {
    final UriLocator uriLocator = factory.getInstance(null);
    Assert.assertNull(uriLocator);
  }

  @Test
  public void testInvalidUri() {
    factory.addUriLocator(new ClasspathUriLocator());
    final UriLocator uriLocator = factory.getInstance("http://www.google.com");
    Assert.assertNull(uriLocator);
  }

  @Test
  public void testValidUri() {
    factory.addUriLocator(new ClasspathUriLocator());
    Assert.assertNotNull(factory.getInstance("classpath:some/classpath/resource.properties"));
  }
}
