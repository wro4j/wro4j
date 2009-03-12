/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.resource;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.UrlUriLocator;

/**
 * Tests if {@link ClasspathUriLocator} works properly.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class TestUrlUriLocator {
  /**
   * UriLocator to test.
   */
  private UriLocator UriLocator;

  @Before
  public void init() {
    UriLocator = new UrlUriLocator();
  }

  @Test
  public void resourceAvailable() throws IOException {
  // final InputStream is = UriLocator
  // .locate("http://localhost:8080/wfc/dwr/interface/UserService.js");
  // IOUtils.copy(is, System.out);
  }
}
