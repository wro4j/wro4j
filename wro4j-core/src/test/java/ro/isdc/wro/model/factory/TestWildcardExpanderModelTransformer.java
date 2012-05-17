/*
 * Copyright (C) 2011 .
 * All rights reserved.
 */
package ro.isdc.wro.model.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FilenameUtils;
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
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer;
import ro.isdc.wro.util.Function;
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
    final BaseWroManagerFactory factory = new BaseWroManagerFactory();
    factory.setProcessorsFactory(processorsFactory);
    factory.addModelTransformer(transformer);
    final Injector injector = InjectorBuilder.create(factory).build();
    injector.inject(transformer);
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
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/expander/file1.js", WroUtil.toPackageAsFolder(getClass()));
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
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/expander/INVALID.*", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);
    final WroModel changedModel = transformer.transform(model);
    Assert.assertEquals(1, changedModel.getGroups().size());
  }

  @Test
  public void testExpandWildcardWithASingleResource() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/expander/?cript1.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: {}", changedModel);
    Assert.assertEquals(1, changedModel.getGroupByName("group").getResources().size());
  }

  @Test
  public void testExpandWildcardWithMultipleResources() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/expander/*.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: {}", changedModel);
    Assert.assertEquals(3, changedModel.getGroupByName("group").getResources().size());
  }

  @Test
  public void testExpandWildcardRootDir() throws Exception {
    final String uri = "/**.js";
    Resource resource = Resource.create(uri, ResourceType.JS);
	Group group = new Group("group").addResource(resource);

    String baseNameFolder = WroUtil.toPackageAsFolder(getClass());
	Function<Collection<File>, Void> expanderHandler = transformer.createExpanderHandler(group, resource, baseNameFolder);
	File mockFile1 = Mockito.mock(File.class);
	Mockito.when(mockFile1.getPath()).thenReturn(baseNameFolder+"/js1.js");
	File mockFile2 = Mockito.mock(File.class);
	Mockito.when(mockFile2.getPath()).thenReturn(baseNameFolder+"/js2.js");
    
	expanderHandler.apply(Arrays.asList(mockFile1, mockFile2));
    LOG.debug("group: {}", group);
    Assert.assertEquals(2, group.getResources().size());
    Assert.assertEquals("/js1.js", group.getResources().get(0).getUri());
    Assert.assertEquals("/js2.js", group.getResources().get(1).getUri());
  }

  @Test
  public void shouldCorrectlyDetectFilesFromFoldersWithDirectoriesOnlyAsChildren() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/expander/subfolder/**.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: {}", changedModel);

    final String resultPathPrefix = String.format("%s%s/expander/subfolder", ClasspathUriLocator.PREFIX, WroUtil.toPackageAsFolder(getClass()));

    Assert.assertEquals(2, changedModel.getGroupByName("group").getResources().size());
    Assert.assertEquals(resultPathPrefix + "/folder1/script1.js", changedModel.getGroupByName("group").getResources().get(0).getUri());
    Assert.assertEquals(resultPathPrefix + "/folder2/script2.js", changedModel.getGroupByName("group").getResources().get(1).getUri());
  }


  @Test
  public void wildcardResourcesAreOrderedAlphabetically() {
    final WroModel model = new WroModel();
    final String uri = String.format(ClasspathUriLocator.PREFIX + "%s/expander/order/**.js", WroUtil.toPackageAsFolder(getClass()));
    model.addGroup(new Group("group").addResource(Resource.create(uri, ResourceType.JS)));
    Mockito.when(decoratedFactory.create()).thenReturn(model);

    final WroModel changedModel = transformer.transform(model);
    LOG.debug("model: {}", changedModel);

    Assert.assertEquals(7, changedModel.getGroupByName("group").getResources().size());
    final List<Resource> resources = changedModel.getGroupByName("group").getResources();

    Assert.assertEquals("01-xyc.js", FilenameUtils.getName(resources.get(0).getUri()));
    Assert.assertEquals("02-xyc.js", FilenameUtils.getName(resources.get(1).getUri()));
    Assert.assertEquals("03-jquery-ui.js", FilenameUtils.getName(resources.get(2).getUri()));
    Assert.assertEquals("04-xyc.js", FilenameUtils.getName(resources.get(3).getUri()));
    Assert.assertEquals("05-xyc.js", FilenameUtils.getName(resources.get(4).getUri()));
    Assert.assertEquals("06-xyc.js", FilenameUtils.getName(resources.get(5).getUri()));
    Assert.assertEquals("07-jquery-impromptu.js", FilenameUtils.getName(resources.get(6).getUri()));
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
