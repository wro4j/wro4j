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

import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;

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
    Mockito.when(context.isDevelopmentMode()).thenReturn(true);
    Context.set(context);
  }

  @Test
  public void testInsertResourceBeforeIsOk() {
    model = buildValidModel();
    Assert.assertFalse(model.getGroups().isEmpty());
    //get first group.
    final Group group = model.getGroups().get(0);
    //create a copy of original list
    final List<Resource> originalResourceList = new ArrayList<Resource>(group.getResources());
    Assert.assertFalse(originalResourceList.isEmpty());
    final Resource resourceToInsert = Resource.create("http://www.site.com/site.css", ResourceType.CSS);
    group.insertResourceBefore(resourceToInsert, originalResourceList.get(0));
    Assert.assertEquals(originalResourceList.size() + 1, group.getResources().size());
    Assert.assertEquals(resourceToInsert, group.getResources().get(0));
  }

  /**
   * @return a valid {@link WroModel} prepopulated with some valid resources.
   */
  private WroModel buildValidModel() {
    final WroModelFactory factory = new XmlModelFactory() {
      @Override
      protected InputStream getConfigResourceAsStream() {
        return Thread.currentThread()
          .getContextClassLoader()
          .getResourceAsStream("wro1.xml");
      }
    };
    //the uriLocator factory doesn't have any locators set...
    final WroModel model = factory.getInstance();
    return model;
  }
}
