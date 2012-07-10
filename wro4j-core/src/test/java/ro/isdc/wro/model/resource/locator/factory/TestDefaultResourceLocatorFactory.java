/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.factory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * Test class for {@link AbstractResourceLocatorFactory}.
 *
 * @author Alex Objelean
 */
public class TestDefaultResourceLocatorFactory {
  private ResourceLocatorFactory factory;
  @Before
  public void setUp() {
    factory = new DefaultResourceLocatorFactory();
  }

  @Test(expected=NullPointerException.class)
  public void testNullUri() throws Exception {
    factory.getLocator(null);
  }

  @Test
  public void testInvalidUri() throws Exception {
    final ResourceLocator locator = factory.getLocator("http://www.google.com");
    Assert.assertEquals(UrlResourceLocator.class, locator.getClass());

  }

  @Test
  public void testValidUri() throws Exception {
    final ResourceLocator locator = factory.getLocator("classpath:" + WroUtil.toPackageAsFolder(TestDefaultResourceLocatorFactory.class));
    Assert.assertEquals(ClasspathResourceLocator.class, locator.getClass());
  }
}
