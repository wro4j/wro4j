/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.RecursiveGroupDefinitionException;
import ro.isdc.wro.exception.WroRuntimeException;
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
  private static final Logger LOG = LoggerFactory.getLogger(TestXmlModelFactory.class);
  private WroModelFactory factory;

  @Before
  public void init() {
    final Context context = Mockito.mock(Context.class);
    Mockito.when(context.isDevelopmentMode()).thenReturn(true);
    Context.set(context);
  }

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
		factory.getInstance();
	}


  @Test(expected = WroRuntimeException.class)
  public void processResourceType() {
    factory = new XmlModelFactory();
    factory.getInstance();
  }

  //TODO use two concurrent calls
  @Test
  public void testTwoConcurrentCreationCalls() {
    testSuccessfulCreation();
    factory.getInstance();
  }

  @Test
  public void testSuccessfulCreation() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream() {
        return Thread.currentThread()
          .getContextClassLoader()
          .getResourceAsStream("wro1.xml");
      }
    };
    //the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.getInstance();
    LOG.debug("model: " + model);
  }
}
