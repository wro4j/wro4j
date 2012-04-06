/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.util.WroTestUtils;

/**
 * Tests if {@link ClasspathUriLocator} works properly.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestClasspathResourceLocator {
  /**
   * UriLocator to test.
   */
  private ClasspathResourceLocator uriLocator;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  private String createUri(final String location) {
    return ClasspathResourceLocator.PREFIX + location;
  }

  @Test
  public void resourceAvailable() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("test.css"));
    Assert.assertNotNull(uriLocator.getInputStream());
  }

  @Test
  public void testRelative() throws Exception {
    uriLocator = new ClasspathResourceLocator(createUri("test.css"));

    ResourceLocator relative = uriLocator.createRelative("1.css");
    Assert.assertNotNull(relative.getInputStream());

    relative = uriLocator.createRelative("ro/../1.css");
    Assert.assertNotNull(relative.getInputStream());
  }

  @Test
  public void resourceAvailableWithTrailingSpaces() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("  test.css  "));
    Assert.assertNotNull(uriLocator.getInputStream());
    Assert.assertNotSame(0, uriLocator.lastModified());
  }

  @Test(expected = IOException.class)
  public void testWildcardInexistentResources() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("*.NOTEXIST"));
    uriLocator.getInputStream();
  }

  @Test
  public void testWildcard1Resources() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("ro/isdc/wro/http/*.merged"));
    WroTestUtils.compare(uriLocator.getInputStream(),
      new ClasspathResourceLocator(createUri("ro/isdc/wro/http/*.css")).getInputStream());
  }

  @Test
  public void testWildcard2Resources() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("ro/isdc/wro/http/*.cs?"));
    Assert.assertNotNull(uriLocator.getInputStream());
    Assert.assertEquals(0, uriLocator.lastModified());
  }

  @Test
  public void testWildcard3Resources() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("ro/isdc/wro/http/*.???"));
    Assert.assertNotNull(uriLocator.getInputStream());
  }

  @Test
  public void testWildcard4Resources() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("ro/isdc/wro/http/**.cs?"));
    Assert.assertNotNull(uriLocator.getInputStream());
  }


  @Test
  public void testRecursiveWildcardResources() throws IOException {
    uriLocator = new ClasspathResourceLocator(createUri("ro/isdc/wro/http/**.css"));
    uriLocator.getInputStream();
  }



  @Test(expected=IOException.class)
  public void resourceUnavailable() throws Exception {
    uriLocator = new ClasspathResourceLocator(createUri("123123.css"));
    uriLocator.getInputStream();
  }
}
