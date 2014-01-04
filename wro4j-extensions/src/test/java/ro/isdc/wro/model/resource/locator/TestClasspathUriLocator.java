package ro.isdc.wro.model.resource.locator;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.factory.ClasspathResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;


/**
 * Tests a special test cases of the {@link ClasspathUriLocator} within the extension module.
 */
public class TestClasspathUriLocator {
  private ResourceLocatorFactory victim;

  @Before
  public void setUp() {
    victim = new ClasspathResourceLocatorFactory();
  }

  @Test
  public void shouldLocateClasspathResourceContainingQuestionMarkInUri()
      throws Exception {
    victim.locate("classpath:META-INF/resources/webjars/font-awesome/4.0.3/fonts/fontawesome-webfont.woff?v=4.0.3");
  }
}