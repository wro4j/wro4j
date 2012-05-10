/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * Tests if {@link UrlUriLocator} works properly.
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
  public void setUp() {
    uriLocator = new UrlUriLocator();
    Context.set(Context.standaloneContext());
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullUri()
      throws IOException {
    uriLocator.locate(null);
  }
  
  @Test(expected = MalformedURLException.class)
  public void cannotLocateMalformedUrl()
      throws IOException {
    uriLocator.locate("/someInvalidUri.html");
  }
  
  @Test
  public void testValidUrl()
      throws IOException {
    uriLocator.locate("http://www.google.com");
  }
  
  @Test(expected = IOException.class)
  public void testWildcardInexistentResources()
      throws IOException {
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
  public void shouldLocateWildcard1Resources()
      throws IOException {
    uriLocator.locate(createUri("*.css"));
  }
  
  @Test
  public void shouldLocateWildcard2Resources()
      throws IOException {
    uriLocator.locate(createUri("*.cs?"));
  }
  
  @Test
  public void shouldLocateWildcard3Resources()
      throws IOException {
    uriLocator.locate(createUri("*.???"));
  }
  
  @Test
  public void shouldLocateRecursiveWildcardResources()
      throws IOException {
    uriLocator.locate(createUri("**.css"));
  }
  
  @Test(expected = IOException.class)
  public void testWildcardUsingInvalidResource()
      throws IOException {
    uriLocator.locate(createUri("http://www.google.com/*.js"));
  }
}
