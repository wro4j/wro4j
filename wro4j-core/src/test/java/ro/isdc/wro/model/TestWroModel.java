/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;
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
    final Context context = Context.standaloneContext();
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

  @Test
  public void testGetGroupNames() {
    model = buildValidModel();
    final List<String> groupNames = model.getGroupNames();
    Collections.sort(groupNames);
    final List<String> expected = Arrays.asList("g1","g2","g3");
    Assert.assertEquals(expected, groupNames);
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
