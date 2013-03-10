/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.model.resource.locator.ResourceLocator;


/**
 * Tests if {@link UrlUriLocator} works properly.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestUrlResourceLocator {
  private ResourceLocator locator;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = MalformedURLException.class)
  public void cannotAcceptEmptyUri()
      throws IOException {
    locator = new UrlResourceLocator("");
    locator.getInputStream();
  }

  @Test(expected = MalformedURLException.class)
  public void cannotLocateMalformedUrl()
      throws IOException {
    locator = new UrlResourceLocator("/someInvalidUri.html");
    locator.getInputStream();
  }

  @Test
  public void testValidUrl()
      throws IOException {
    locator = new UrlResourceLocator("http://www.google.com");
    locator.getInputStream();
  }

  @Test
  public void testWildcardInexistentResources()
      throws IOException {
    locator = new UrlResourceLocator("http://www.google.com");
    locator.getInputStream();
  }

  private String createUri(final String uri) {
    return createUri(uri, "ro/isdc/wro/model/resource/locator/");
  }

  private String createUri(final String uri, final String path) {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    return url.getProtocol() + ":" + url.getPath() + uri;
  }

  @Test
  public void testWildcard1Resources()
      throws IOException {
    locator = new UrlResourceLocator(createUri("*.css"));
    locator.getInputStream();
  }

  @Test
  public void testWildcard2Resources()
      throws IOException {
    locator = new UrlResourceLocator(createUri("*.cs?"));
    locator.getInputStream();
  }

  @Test
  public void testWildcard3Resources()
      throws IOException {
    locator = new UrlResourceLocator(createUri("*.???"));
    locator.getInputStream();
  }

  @Test
  public void testRecursiveWildcardResources()
      throws IOException {
    locator = new UrlResourceLocator(createUri("**.css"));
    locator.getInputStream();
  }

  @Test(expected = IOException.class)
  public void testWildcardUsingInvalidResource()
      throws IOException {
    locator = new UrlResourceLocator(createUri("http://www.google.com/*.js"));
    locator.getInputStream();
  }


  @Test
  public void shouldFindWildcardResourcesForFolderContainingSpaces()
      throws IOException {
    locator = new UrlResourceLocator(createUri("/folder with spaces/**.css", "test"));
    locator.getInputStream();
  }
}
