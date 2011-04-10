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
 * Test class for {@link DefaultResourceLocatorFactory}.
 *
 * @author Alex Objelean
 */
public class TestDefaultResourceLocatorFactory {
  private ResourceLocatorFactory factory;
  @Before
  public void setUp() {
    factory = DefaultResourceLocatorFactory.contextAwareFactory();
  }

  @Test(expected=IllegalArgumentException.class)
  public void testNullUri() throws Exception {
    factory.locate(null);
  }

  @Test
  public void testInvalidUri() throws Exception {
    final ResourceLocator locator = factory.locate("http://www.google.com");
    Assert.assertEquals(UrlResourceLocator.class, locator.getClass());

  }

  @Test
  public void testValidUri() throws Exception {
    final ResourceLocator locator = factory.locate("classpath:" + WroUtil.toPackageAsFolder(TestDefaultResourceLocatorFactory.class));
    Assert.assertEquals(ClasspathResourceLocator.class, locator.getClass());
  }
}
