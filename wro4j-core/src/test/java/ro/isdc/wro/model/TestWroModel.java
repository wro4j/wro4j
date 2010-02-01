/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;
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
  public void testInsertResourceBeforeIsOk() {
    model = buildValidModel();
    Assert.assertFalse(model.getGroups().isEmpty());
    //get first group.
    final Group group = model.getGroups().iterator().next();
    //create a copy of original list
    final List<Resource> originalResourceList = new ArrayList<Resource>(group.getResources());
    Assert.assertFalse(originalResourceList.isEmpty());
    Resource resourceToInsert = Resource.create("http://www.site.com/site1.css", ResourceType.CSS);

    group.insertResourceBefore(resourceToInsert, group.getResources().get(0));
    resourceToInsert.setGroup(group);

    Assert.assertEquals(originalResourceList.size() + 1, group.getResources().size());
    Assert.assertEquals(resourceToInsert, group.getResources().get(0));

    resourceToInsert = Resource.create("http://www.site.com/site2.css", ResourceType.CSS);
    resourceToInsert.setGroup(group);
    group.getResources().get(0).prepend(resourceToInsert);

    Assert.assertEquals(originalResourceList.size() + 2, group.getResources().size());
    Assert.assertEquals(resourceToInsert, group.getResources().get(0));
  }

  /**
   * @return a valid {@link WroModel} prepopulated with some valid resources.
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
