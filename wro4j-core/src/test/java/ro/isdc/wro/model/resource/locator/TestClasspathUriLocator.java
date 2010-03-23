/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests if C works properly.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestClasspathUriLocator {
  /**
   * UriLocator to test.
   */
  private UriLocator uriLocator;

  @Before
  public void init() {
    uriLocator = new ClasspathUriLocator();
  }

  private String createUri(final String location) {
    return ClasspathUriLocator.PREFIX + location;
  }

  @Test
  public void resourceAvailable() throws IOException {
    uriLocator.locate(createUri("test.css"));
  }

  @Test
  public void resourceUnavailable() {
    try {
      uriLocator.locate(createUri("123123.css"));
      Assert.fail("Should throw exception");
    } catch (final IOException e) {
      // TODO: handle exception
    }
  }
}
