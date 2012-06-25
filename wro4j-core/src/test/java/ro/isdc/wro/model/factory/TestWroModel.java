/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.DefaultContext;
import ro.isdc.wro.model.WroModel;
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
    final DefaultContext context = DefaultContext.standaloneContext();
    DefaultContext.set(context);
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
  
  @Test
  public void testGetGroupNames() {
    final List<String> groupNames = victim.getGroupNames();
    Collections.sort(groupNames);
    final List<String> expected = Arrays.asList("g1", "g2", "g3");
    Assert.assertEquals(expected, groupNames);
  }
  
  @Test(expected = InvalidGroupNameException.class)
  public void testGetInvalidGroup() {
    Assert.assertFalse(victim.getGroups().isEmpty());
    victim.getGroupByName("INVALID_GROUP");
  }
  
  @Test
  public void shouldReturnAllResourcesFromModel() {
    assertEquals(3, victim.getAllResources().size());
  }
  
  @Test
  public void shouldNotReturnDuplicatedResources() {
    victim = new WroModel();
    assertEquals(0, victim.getAllResources().size());
    
    victim.addGroup(new Group("one").addResource(Resource.create("/one.js"))).addGroup(
        new Group("two").addResource(Resource.create("/one.js")));
    assertEquals(1, victim.getAllResources().size());
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
  public void shouldReturnEmptyCollectionWhenAResourceIsNotContainedInAnyGroup() {
    assertTrue(victim.getGroupNamesContainingResource("/someResource.js").isEmpty());
  }
  
  @Test
  public void shouldFindTheGroupContainingResource() {
    Collection<String> groups = victim.getGroupNamesContainingResource("/path/to/resource");
    assertEquals(2, groups.size());
    assertEquals("[g2, g3]", Arrays.toString(groups.toArray()));
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotGetGroupsUsingNullResource() {
    victim.getGroupNamesContainingResource(null);
  }
}
