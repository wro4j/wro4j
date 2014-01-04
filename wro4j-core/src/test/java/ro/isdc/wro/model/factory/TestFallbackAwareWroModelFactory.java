/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;


/**
 * Test class for {@link FallbackAwareXmlModelFactory}.
 *
 * @author Alex Objelean
 */
public class TestFallbackAwareWroModelFactory {
  private WroModelFactory xmlModelFactory;
  private WroModelFactory fallbackAwareModelFactory;
  /**
   * Used to simulate that a resource stream used to build the model is not available.
   */
  private boolean isModelAvailable;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  @Before
  public void setUp() {
    isModelAvailable = true;
    // initialize the context
    Context.set(Context.standaloneContext());
    final ResourceLocator locator = new UrlResourceLocator(TestFallbackAwareWroModelFactory.class.getResource("wro.xml")) {
      @Override
      public InputStream getInputStream() throws IOException {
        if (isModelAvailable) {
          return super.getInputStream();
        }
        isModelAvailable = !isModelAvailable;
        return null;
      };
    };
    fallbackAwareModelFactory = new FallbackAwareWroModelFactory(new XmlModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return locator;
      }
    });
    xmlModelFactory = new XmlModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return locator;
      }
    };
  }

  @Test
  public void testLastValidIsOK() {
    Assert.assertNotNull(fallbackAwareModelFactory.create());
    fallbackAwareModelFactory.destroy();
    Assert.assertNotNull(fallbackAwareModelFactory.create());
  }

  @Test(expected = WroRuntimeException.class)
  public void testWithoutLastValidThrowsException() {
    isModelAvailable = false;
    Assert.assertNotNull(xmlModelFactory.create());
  }

  @After
  public void tearDown() {
    fallbackAwareModelFactory.destroy();
    xmlModelFactory.destroy();
  }
}
