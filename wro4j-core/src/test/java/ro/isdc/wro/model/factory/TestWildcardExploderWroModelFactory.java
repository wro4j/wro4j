/*
 * Copyright (C) 2011 .
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.WroUtil;

/**
 * @author Alex Objelean
 */
public class TestWildcardExploderWroModelFactory {
  private WildcardExploderWroModelFactory factory;
  @Mock
  private WroModelFactory decoratedFactory;
  @Mock
  private ProcessorsFactory processorsFactory;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    final UriLocatorFactory uriLocatorFactory = new DefaultUriLocatorFactory();
    final Injector injector = new Injector(uriLocatorFactory, processorsFactory);

    factory = new WildcardExploderWroModelFactory(decoratedFactory);

    injector.inject(factory);
  }

  @Test
  public void test() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/exploder/*.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = factory.create();
    System.out.println(changedModel);
  }
}
