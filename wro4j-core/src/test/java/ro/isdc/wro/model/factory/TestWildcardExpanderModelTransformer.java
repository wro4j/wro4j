/*
 * Copyright (C) 2011 .
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.DefaultResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.ClasspathResourceLocator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer;
import ro.isdc.wro.util.WroUtil;

/**
 * @author Alex Objelean
 */
public class TestWildcardExpanderModelTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(TestWildcardExpanderModelTransformer.class);
  private WildcardExpanderModelTransformer transformer;
  @Mock
  private WroModelFactory decoratedFactory;
  @Mock
  private ProcessorsFactory processorsFactory;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.standaloneContext());
    transformer = new WildcardExpanderModelTransformer();
    //create manager to force correct initialization.
    new BaseWroManagerFactory().setProcessorsFactory(processorsFactory).addModelTransformer(transformer).create();
  }

  @Test
  public void testEmptyModel() {
    final WroModel model = new WroModel();
    Mockito.when(decoratedFactory.create()).thenReturn(model);
    final WroModel changedModel = transformer.transform(model);
    Assert.assertEquals(model.getGroups().size(), changedModel.getGroups().size());
  }

  @Test
  public void testGroupWithNoWildcard() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathResourceLocator.PREFIX + "%s/expander/file1.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);
    final WroModel changedModel = transformer.transform(model);
    Assert.assertEquals(1, changedModel.getGroups().size());
  }

  /**
   * Invalid resources should be ignored, leaving the model unchanged.
   */
  @Test
  public void testGroupWithInvalidResource() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathResourceLocator.PREFIX + "%s/expander/INVALID.*", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);
    final WroModel changedModel = transformer.transform(model);
    Assert.assertEquals(1, changedModel.getGroups().size());
  }

  @Test
  public void testExpandWildcardWithASingleResource() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathResourceLocator.PREFIX + "%s/expander/?cript1.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: " + changedModel);
    Assert.assertEquals(1, changedModel.getGroupByName("group").getResources().size());
  }

  @Test
  public void testExpandWildcardWithMultipleResources() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathResourceLocator.PREFIX + "%s/expander/*.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: " + changedModel);
    Assert.assertEquals(3, changedModel.getGroupByName("group").getResources().size());
  }

  @Test
  public void shouldCorrectlyDetectFilesFromFoldersWithDirectoriesOnlyAsChildren() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathResourceLocator.PREFIX + "%s/expander/subfolder/**.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: " + changedModel);

    final String resultPathPrefix = String.format("%s%s/expander/subfolder", ClasspathResourceLocator.PREFIX, WroUtil.toPackageAsFolder(getClass()));

    Assert.assertEquals(2, changedModel.getGroupByName("group").getResources().size());
    Assert.assertEquals(resultPathPrefix + "/folder1/script1.js", changedModel.getGroupByName("group").getResources().get(0).getUri());
    Assert.assertEquals(resultPathPrefix + "/folder2/script2.js", changedModel.getGroupByName("group").getResources().get(1).getUri());
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
