/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Before;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.resource.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.util.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * AbstractWroTest.java. TODO this class is a copy from core tests. Find a way to reuse it.
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
    // initialize the factory through manager, in order to let the Injector scan @Inject annotations.
    new WroManager() {
      @Override
      protected UriLocatorFactory newUriLocatorFactory() {
        uriLocatorFactory = new SimpleUriLocatorFactory().addUriLocator(new ServletContextUriLocator()).addUriLocator(
            new ClasspathUriLocator()).addUriLocator(new UrlUriLocator());
        return uriLocatorFactory;
      }
    };
  }


  /**
   * Compare contents of two resources (files) by performing some sort of processing on input resource.
   *
   * @param inputResourceUri uri of the resource to process.
   * @param expectedContentResourceUri uri of the resource to compare with processed content.
   * @param processor a closure used to process somehow the input content.
   */
  protected void compareProcessedResourceContents(final String inputResourceUri,
    final String expectedContentResourceUri, final ResourceProcessor processor)
    throws IOException {
    final Reader resultReader = getReaderFromUri(inputResourceUri);
    final Reader expectedReader = getReaderFromUri(expectedContentResourceUri);
    WroTestUtils.compare(resultReader, expectedReader, processor);
  }

  /**
   * Used to compare streamToProcess with expectedStream after processing the first with the supplied processor.
   * @param streamToProcess {@link InputStream} to be processed by processor.
   * @param expectedStream {@link InputStream} of the expected content after processing is done.
   * @param processor {@link ResourceProcessor} to apply on streamToProcess.
   */
  protected void compareProcessedResourceContents(final InputStream streamToProcess,
    final InputStream expectedStream, final ResourceProcessor processor)
    throws IOException {
    final Reader readerToProcess = new InputStreamReader(streamToProcess);
    final Reader expectedReader = new InputStreamReader(expectedStream);
    WroTestUtils.compare(readerToProcess, expectedReader, processor);
  }


  private Reader getReaderFromUri(final String uri)
    throws IOException {
    // wrap reader with bufferedReader for top efficiency
    return new BufferedReader(new InputStreamReader(uriLocatorFactory.locate(uri), "UTF-8"));
  }
}
