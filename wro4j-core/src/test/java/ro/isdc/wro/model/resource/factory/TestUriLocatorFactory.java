/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.factory;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * Test class for {@link IUriLocatorFactory}.
 *
 * @author Alex Objelean
 */
public class TestUriLocatorFactory {
  private UriLocatorFactory factory;
  @Before
  public void setUp() {
    final GroupsProcessor groupsProcessor = new GroupsProcessor() {
      @Override
      protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
        TestUriLocatorFactory.this.factory = factory;
      }
    };
  }

  @Test(expected=IOException.class)
  public void testNullUri() throws Exception {
    factory.locate(null);
  }

  @Test(expected=IOException.class)
  public void testInvalidUri() throws Exception {
    factory.addUriLocator(new ClasspathUriLocator());
    factory.locate("http://www.google.com");
  }

  @Test
  public void testValidUri() throws Exception {
    factory.addUriLocator(new ClasspathUriLocator());
    Assert.assertNotNull(factory.locate("classpath:" + WroUtil.toPackageAsFolder(TestUriLocatorFactory.class)));
  }
}
