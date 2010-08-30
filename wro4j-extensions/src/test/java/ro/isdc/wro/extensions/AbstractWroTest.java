/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Before;

import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.test.util.ResourceProcessor;
import ro.isdc.wro.test.util.WroTestUtils;

/**
 * AbstractWroTest.java. TODO this class is a copy from core tests. Find a way
 * to reuse it.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public abstract class AbstractWroTest {
  /**
   * UriLocator Factory.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Create UriLocatorfactory and set uriLocators to be used for tests.
   */
  @Before
  public void initUriLocatorFactory() {
    uriLocatorFactory = new UriLocatorFactory();
    // populate the list. The order is important.
    uriLocatorFactory.addUriLocator(new ServletContextUriLocator());
    uriLocatorFactory.addUriLocator(new ClasspathUriLocator());
    uriLocatorFactory.addUriLocator(new UrlUriLocator());
  }

  /**
   * Compare contents of two resources (files) by performing some sort of
   * processing on input resource.
   *
   * @param inputResourceUri
   *          uri of the resource to process.
   * @param expectedContentResourceUri
   *          uri of the resource to compare with processed content.
   * @param processor
   *          a closure used to process somehow the input content.
   */
  protected void compareProcessedResourceContents(
      final String inputResourceUri, final String expectedContentResourceUri,
      final ResourceProcessor processor) throws IOException {
    final Reader resultReader = getReaderFromUri(inputResourceUri);
    final Reader expectedReader = getReaderFromUri(expectedContentResourceUri);
    WroTestUtils.compareProcessedResourceContents(resultReader, expectedReader,
        processor);
  }

  /**
   * @param expectedContentResourceUri
   * @return
   */
  private Reader getReaderFromUri(final String uri) throws IOException {
    // wrap reader with bufferedReader for top efficiency
    return new BufferedReader(new InputStreamReader(uriLocatorFactory
        .getInstance(uri).locate(uri), "UTF-8"));
  }
}
