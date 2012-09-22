/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model;

import static junit.framework.Assert.assertEquals;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;
import ro.isdc.wro.model.resource.Resource;


/**
 * Test class for WroModel..
 * 
 * @author Alex Objelean
 * @created Created on Jan 6, 2010
 */
public class TestWroModel {
  private WroModel victim;
  private WroModelFactory factory;
  
  @Before
  public void setUp() {
    final Context context = Context.standaloneContext();
    Context.set(context);
    victim = buildValidModel();
  }
  
  @After
  public void tearDown() {
    factory.destroy();
  }
  
  @Test
  public void testGetExistingGroup() {
    Assert.assertFalse(victim.getGroups().isEmpty());
    final Group group = victim.getGroupByName("g1");
    // create a copy of original list
    Assert.assertEquals(1, group.getResources().size());
  }
  
  
  @Test(expected = InvalidGroupNameException.class)
  public void testGetInvalidGroup() {
    Assert.assertFalse(victim.getGroups().isEmpty());
    victim.getGroupByName("INVALID_GROUP");
  }
  
  /**
   * @return a valid {@link WroModel} pre populated with some valid resources.
   */
  private WroModel buildValidModel() {
    factory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        return getClass().getResourceAsStream("wro.xml");
      }
    };
    // the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.create();
    return model;
  }
  
  @Test
  public void shouldNotReturnDuplicatedResources() {
    final WroModel model = new WroModel();
    
    assertEquals(0, new WroModelInspector(model).getAllUniqueResources().size());
    
    model.addGroup(new Group("one").addResource(Resource.create("/one.js"))).addGroup(
        new Group("two").addResource(Resource.create("/one.js")));
    assertEquals(1, new WroModelInspector(model).getAllUniqueResources().size());
  }

}
