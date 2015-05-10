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
import ro.isdc.wro.util.WroTestUtils;


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
  private UriLocator victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    victim = new UrlUriLocator();
    WroTestUtils.createInjector().inject(victim);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullUri()
      throws IOException {
    victim.locate(null);
  }

  @Test(expected = MalformedURLException.class)
  public void cannotLocateMalformedUrl()
      throws IOException {
    victim.locate("/someInvalidUri.html");
  }

  @Test
  public void testValidUrl()
      throws IOException {
    victim.locate("http://www.google.com");
  }

  @Test(expected = IOException.class)
  public void testWildcardInexistentResources()
      throws IOException {
    victim.locate(createUri("*.NOTEXIST"));
  }

  private String createUri(final String uri) {
    return createUri(uri, "ro/isdc/wro/model/resource/locator/");
  }

  private String createUri(final String uri, final String path) {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    return url.getProtocol() + ":" + url.getPath() + uri;
  }

  @Test
  public void shouldLocateWildcard1Resources()
      throws IOException {
    victim.locate(createUri("*.css"));
  }

  @Test
  public void shouldLocateWildcard2Resources()
      throws IOException {
    victim.locate(createUri("*.cs?"));
  }

  @Test
  public void shouldLocateWildcard3Resources()
      throws IOException {
    victim.locate(createUri("*.???"));
  }

  @Test
  public void shouldLocateRecursiveWildcardResources()
      throws IOException {
    victim.locate(createUri("**.css"));
  }

  @Test(expected = IOException.class)
  public void testWildcardUsingInvalidResource()
      throws IOException {
    victim.locate(createUri("http://www.google.com/*.js"));
  }

  @Test
  public void shouldFindWildcardResourcesForFolderContainingSpaces()
      throws IOException {
    victim.locate(createUri("/folder with spaces/**.css", "test"));
  }
  
  /**
   * The requested endpoint returns 403 status code when request doesn't contain agent header.
   */
  @Test
  public void shouldRetrieveCDNResourceRequiringAgentHeader() throws IOException {
	  victim.locate("http://cdn.datatables.net/1.10.5/js/jquery.dataTables.min.js");
  }
}
