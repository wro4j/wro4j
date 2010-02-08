/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.resource;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * Tests if C works properly.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
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

  @Test
  public void test() {

  }

  public static void main(final String[] args) throws Exception {
    final File file = new File("/f1/f2/../img/test.jpg");
    System.out.println(file.getCanonicalPath());
  }
}
