/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.PreProcessorExecutor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

/**
 * TestGroupsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestGroupsProcessor {
  private GroupsProcessor groupsProcessor;

  @Before
  public void init() {
    groupsProcessor = new GroupsProcessor();
  }

  @Test
  public void testNoProcessorsSet() {
  	Assert.assertTrue(groupsProcessor.getPostProcessorsByType(null).isEmpty());
  }

  /**
   * Test if getPostProcessorsByType is implemented correctly
   */
  @Test
  public void testMixedPreProcessors() {
    final Collection<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
    processors.add(new CssMinProcessor());
    processors.add(new BomStripperPreProcessor());
    groupsProcessor.setResourcePreProcessors(processors);
    Assert.assertEquals(1, groupsProcessor.getPreProcessorsByType(null).size());
    Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
    Assert.assertEquals(1, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
  }

  /**
   * Test if getPostProcessorsByType is implemented correctly
   */
  @Test
  public void testMixedPostProcessors() {
    final Collection<ResourcePostProcessor> processors = new ArrayList<ResourcePostProcessor>();
    processors.add(new CssMinProcessor());
    processors.add(new CommentStripperProcessor());
    groupsProcessor.setResourcePostProcessors(processors);
    Assert.assertEquals(1, groupsProcessor.getPostProcessorsByType(null).size());
    Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
    Assert.assertEquals(1, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
  }

  /**
   * Test if getPostProcessorsByType is implemented correctly
   */
  @Test
  public void testGetPostProcessorsByType1() {
  	final Collection<ResourcePostProcessor> processors = new ArrayList<ResourcePostProcessor>();
  	processors.add(new CssMinProcessor());
  	processors.add(new JSMinProcessor());
  	processors.add(new CssVariablesProcessor());
  	groupsProcessor.setResourcePostProcessors(processors);
  	Assert.assertEquals(0, groupsProcessor.getPostProcessorsByType(null).size());
  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
  	Assert.assertEquals(1, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
  }

  @Test
  public void testGetPostProcessorsByNullType2() {
  	final Collection<ResourcePostProcessor> processors = new ArrayList<ResourcePostProcessor>();
  	processors.add(new MultiLineCommentStripperProcessor());
  	processors.add(new SingleLineCommentStripperProcessor());
  	groupsProcessor.setResourcePostProcessors(processors);
  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(null).size());
  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
  }

  @Test
  public void testGetPreProcessorsByNullType1() {
  	final Collection<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
  	processors.add(new CssMinProcessor());
  	processors.add(new JSMinProcessor());
  	processors.add(new CssVariablesProcessor());
  	groupsProcessor.setResourcePreProcessors(processors);
  	Assert.assertEquals(0, groupsProcessor.getPreProcessorsByType(null).size());
  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
  	Assert.assertEquals(1, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
  }

  @Test
  public void testGetPreProcessorsByNullType2() {
  	final Collection<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
  	processors.add(new MultiLineCommentStripperProcessor());
  	processors.add(new SingleLineCommentStripperProcessor());
  	groupsProcessor.setResourcePreProcessors(processors);
  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(null).size());
  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
  }

  @Test
  public void injectAnnotationOnPreProcessorField() {
    final UriLocatorFactory uriLocatorFactory = groupsProcessor.getUriLocatorFactory();
    groupsProcessor.addPreProcessor(new ResourcePreProcessor() {
      @Inject
      private UriLocatorFactory factory;
      @Inject
      private PreProcessorExecutor preProcessorExecutor;
      public void process(final Resource resources, final Reader reader, final Writer writer)
        throws IOException {
        Assert.assertEquals(uriLocatorFactory, factory);
        Assert.assertNotNull(preProcessorExecutor);
      }
    });
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInjectOnInvalidFieldOfPreProcessor() {
    groupsProcessor.addPreProcessor(new ResourcePreProcessor() {
      @Inject
      private Object someObject;
      public void process(final Resource resources, final Reader reader, final Writer writer)
        throws IOException {
      }
    });
  }

  @Test(expected=IllegalArgumentException.class)
  public void cannotAcceptNullArguments() {
    groupsProcessor.process(null, null, true);
  }

  @Test
  public void injectAnnotationOnPostProcessorField() {
    final UriLocatorFactory uriLocatorFactory = groupsProcessor.getUriLocatorFactory();
    groupsProcessor.addPostProcessor(new ResourcePostProcessor() {
      @Inject
      private UriLocatorFactory factory;
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        Assert.assertEquals(uriLocatorFactory, factory);
      }
    });
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInjectOnInvalidFieldOfPostProcessor() {
    groupsProcessor.addPostProcessor(new ResourcePostProcessor() {
      @Inject
      private Object someObject;
      public void process(final Reader reader, final Writer writer)
        throws IOException {
      }
    });
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotAddProcessorBeforeSettingUriLocatorFactory() {
    groupsProcessor.addPostProcessor(new ResourcePostProcessor() {
      @Inject
      private Object someObject;
      public void process(final Reader reader, final Writer writer)
        throws IOException {
      }
    });
  }

  /**
   * Check if minimize aware processor is not called when minimization is not wanted.
   * @throws Exception
   */
  @Test
  public void testMinimizeAwareProcessorIsNotCalled() throws Exception {
    final ResourcePostProcessor postProcessor = getMinimizeAwareProcessorWithMinimizeSetTo(false);
    Mockito.verify(postProcessor, Mockito.times(0)).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
  }

//  @Test
//  public void testGroupHashCode() {
//    final Group group = new Group();
//    final Resource resource = Resource.create("classpath:ro/isdc/wro/processor/cssImports/test1-input.css", ResourceType.CSS);
//    group.setResources(Arrays.asList(resource));
//    Assert.assertEquals(0, resource.hashCode());
//    Assert.assertEquals(0, group.hashCode());
//  }

  /**
   * Creates a mocked {@link ResourcePostProcessor} object used to check how many times it was invoked depending on
   * minimize flag.
   *
   * @return {@link ResourcePostProcessor} mock object.
   */
  private ResourcePostProcessor getMinimizeAwareProcessorWithMinimizeSetTo(final boolean minimize) {
    final Group group = new Group();
    group.setResources(Arrays.asList(Resource.create("classpath:ro/isdc/wro/processor/cssImports/test1-input.css", ResourceType.CSS)));
    final List<Group> groups = Arrays.asList(group);
    groupsProcessor = new GroupsProcessor() {
      @Override
      protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
        factory.addUriLocator(new ClasspathUriLocator());
      };
    };

    final ResourcePostProcessor postProcessor = Mockito.mock(JawrCssMinifierProcessor.class);
    groupsProcessor.addPostProcessor(postProcessor);
    groupsProcessor.process(groups, ResourceType.CSS, minimize);
    return postProcessor;
  }


  /**
   * Check if minimize aware processor is called when minimization is wanted.
   * @throws Exception
   */
  @Test
  public void testMinimizeAwareProcessorIsCalled() throws Exception {
    final ResourcePostProcessor postProcessor = getMinimizeAwareProcessorWithMinimizeSetTo(true);
    Mockito.verify(postProcessor, Mockito.times(1)).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
  }

  @Test
  public void testGroupWithCssImportProcessor() throws Exception {
    final Group group = new Group();
    group.setResources(Arrays.asList(Resource.create("classpath:ro/isdc/wro/processor/cssImports/test1-input.css", ResourceType.CSS)));
    final List<Group> groups = Arrays.asList(group);
    groupsProcessor = new GroupsProcessor() {
      @Override
      protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
        factory.addUriLocator(new ClasspathUriLocator());
      };
    };
    groupsProcessor.addPreProcessor(new CssImportPreProcessor());
    final ResourcePreProcessor preProcessor = Mockito.mock(ResourcePreProcessor.class);
    groupsProcessor.addPreProcessor(preProcessor);
    groupsProcessor.process(groups, ResourceType.CSS, true);
    Mockito.verify(preProcessor, Mockito.times(6)).process(Mockito.any(Resource.class), Mockito.any(Reader.class), Mockito.any(Writer.class));
  }

}
