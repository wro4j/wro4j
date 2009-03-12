/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.exception.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;

/**
 * TestProcessor.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class TestXmlModelFactory {
  private WroModelFactory factory;

  @Before
  public void init() {}

  @Test
  public void recursiveGroupThrowsException() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream() {
        return Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("recursive.xml");
      }
    };
    try {
      final UriLocatorFactoryImpl uriLocatorFactory = new UriLocatorFactoryImpl();
      // add classpathUriLocator, because we will test against a resource in the
      // classpath
      uriLocatorFactory.addUriLocator(new ClasspathUriLocator());
      final WroModel model = factory.getInstance(uriLocatorFactory);
      Assert.fail("Should have thrown "
          + RecursiveGroupDefinitionException.class);
    } catch (final RecursiveGroupDefinitionException e) {

    }
  }

  @Test
  public void processResourceType() {
  // factory = new XmlModelFactory();
  // final WroModel model = factory.getInstance(new UriLocatorFactoryImpl());
  // System.out.println(model);
  }

  @Test
  public void processResourceType1() {
  // factory = new XmlModelFactory() {
  // @Override
  // protected InputStream getConfigResourceAsStream() {
  // return Thread.currentThread().getContextClassLoader()
  // .getResourceAsStream("wro1.xml");
  // }
  // };
  // final WroModel model = factory.getInstance(new UriLocatorFactoryImpl());
  // System.out.println(model);
  }
}
