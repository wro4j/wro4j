/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.exception.InvalidGroupNameException;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.test.util.WroTestUtils;

/**
 * Test class for WroModel..
 *
 * @author Alex Objelean
 * @created Created on Jan 6, 2010
 */
public class TestWroModel {
  private WroModel model;

  @Before
  public void init() {
    final Context context = Mockito.mock(Context.class);
    Context.set(context);
  }

  @Test
  public void testGetExistingGroup() {
    model = buildValidModel();
    Assert.assertFalse(model.getGroups().isEmpty());
    final Group group = model.getGroupByName("g1");
    //create a copy of original list
    Assert.assertEquals(1, group.getResources().size());
  }

  @Test(expected=InvalidGroupNameException.class)
  public void testGetInvalidGroup() {
    model = buildValidModel();
    Assert.assertFalse(model.getGroups().isEmpty());
    model.getGroupByName("INVALID_GROUP");
  }

  /**
   * @return a valid {@link WroModel} pre populated with some valid resources.
   */
  private WroModel buildValidModel() {
    final WroModelFactory factory = new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream() {
        return WroTestUtils.getClassRelativeResource(TestWroModel.class, "wro.xml");
      }
    };
    //the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.getInstance();
    return model;
  }
}
