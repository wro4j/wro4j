/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;


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
  private boolean flag = false;


  @Before
  public void setUp() {
    // initialize the context
    Context.set(Context.standaloneContext());
    fallbackAwareModelFactory = new ScheduledWroModelFactory(new FallbackAwareWroModelFactory(new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        flag = !flag;
        if (flag) {
          return super.getConfigResourceAsStream();
        }
        return null;
      }
    }));
    xmlModelFactory = new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream()
        throws IOException {
        if (flag) {
          return super.getConfigResourceAsStream();
        }
        flag = !flag;
        return null;
      }
    };
  }


  @After
  public void tearDown() {
    fallbackAwareModelFactory.destroy();
    xmlModelFactory.destroy();
  }


  @Test
  public void testLastValidIsOK() {
    Assert.assertNotNull(fallbackAwareModelFactory.create());
    ((WroConfigurationChangeListener)fallbackAwareModelFactory).onModelPeriodChanged();
    Assert.assertNotNull(fallbackAwareModelFactory.create());
  }


  @Test(expected = WroRuntimeException.class)
  public void testWithoutLastValidThrowsException() {
    Assert.assertNotNull(xmlModelFactory.create());
  }
}
