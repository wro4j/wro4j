/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.util.WroUtil;


/**
 * Test class for {@link SimpleUriLocatorFactory}.
 * 
 * @author Alex Objelean
 */
public class TestUriLocatorFactory {
  private SimpleUriLocatorFactory factory;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    factory = new SimpleUriLocatorFactory();
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotLocateNullUri()
      throws Exception {
    factory.locate(null);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenNoCapableLocatorAvailable()
      throws Exception {
    factory.addLocator(new ClasspathUriLocator());
    factory.locate("http://www.google.com");
  }
  
  @Test
  public void testValidUri()
      throws Exception {
    factory.addLocator(new ClasspathUriLocator());
    assertNotNull(factory.locate("classpath:" + WroUtil.toPackageAsFolder(TestUriLocatorFactory.class)));
  }
}
