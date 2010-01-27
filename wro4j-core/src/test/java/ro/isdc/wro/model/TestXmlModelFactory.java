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

import ro.isdc.wro.config.ApplicationSettings;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.exception.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.impl.XmlModelFactory;

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
    Mockito.when(context.getApplicationSettings()).thenReturn(new ApplicationSettings());
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
		factory.getInstance();
	}
//
//
//  @Test
//  public void processResourceType() {
//    factory = new XmlModelFactory();
//    factory.getInstance();
//  }

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
