/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.exception.RecursiveGroupDefinitionException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;

/**
 * TestProcessor.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestXmlModelFactory {
  private WroModelFactory factory;

  @Before
  public void init() {}

  @Test(expected=RecursiveGroupDefinitionException.class)
  public void recursiveGroupThrowsException() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream() {
        return Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("recursive.xml");
      }
		};
		final UriLocatorFactoryImpl uriLocatorFactory = new UriLocatorFactoryImpl();
		// add classpathUriLocator, because we will test against a resource in
		// the classpath
		uriLocatorFactory.addUriLocator(new ClasspathUriLocator());
		final Context context = Mockito.mock(Context.class);
		Mockito.when(context.isDevelopmentMode()).thenReturn(true);
		Context.set(context);
		factory.getInstance();
	}

//  @Test
//  public void processResourceType() {
//   factory = new XmlModelFactory();
//   final WroModel model = factory.getInstance(new UriLocatorFactoryImpl());
//   System.out.println(model);
//  }

  //This test doesn't make sense after the refactoring
//  @Test(expected = WroRuntimeException.class)
//  public void cannotProcessResourceWithoutUriLocators() {
//    factory = new XmlModelFactory() {
//      @Override
//      protected InputStream getConfigResourceAsStream() {
//        return Thread.currentThread()
//          .getContextClassLoader()
//          .getResourceAsStream("wro1.xml");
//      }
//    };
//    //the uriLocator factory doesn't have any locators set...
//    factory.getInstance();
//  }
}
