/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.resource;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.UrlUriLocator;

/**
 * Tests if {@link ClasspathUriLocator} works properly.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestUrlUriLocator {
  /**
   * UriLocator to test.
   */
  private UriLocator uriLocator;

  @Before
  public void init() {
    uriLocator = new UrlUriLocator();
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotAcceptNullUri() throws IOException {
    uriLocator.locate(null);
  }

  @Test(expected=MalformedURLException.class)
  public void cannotLocateMalformedUrl()
    throws IOException {
    uriLocator.locate("/someInvalidUri.html");
  }
}
