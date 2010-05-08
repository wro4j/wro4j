/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

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

  @Test
  public void testValidUrl()
    throws IOException {
    uriLocator.locate("http://www.google.com");
  }

  @Test
  public void testWildcardInexistentResources() throws IOException {
    uriLocator.locate(createUri("*.NOTEXIST"));
  }

  /**
   * @param string
   * @return
   */
  private String createUri(final String uri) {
    final URL url = Thread.currentThread().getContextClassLoader().getResource("ro/isdc/wro/model/resource/locator/");
    return url.getProtocol() + ":" + url.getPath() + uri;
  }

  @Test
  public void testWildcard1Resources() throws IOException {
    uriLocator.locate(createUri("*.css"));
  }

  @Test
  public void testWildcard2Resources() throws IOException {
    uriLocator.locate(createUri("*.cs?"));
  }

  @Test
  public void testWildcard3Resources() throws IOException {
    uriLocator.locate(createUri("*.???"));
  }

  @Test
  public void testRecursiveWildcardResources() throws IOException {
    uriLocator.locate(createUri("**.css"));
  }

}
