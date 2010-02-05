/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.annot.Inject;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.processor.impl.CssMinProcessor;
import ro.isdc.wro.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.JSMinProcessor;
import ro.isdc.wro.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * TestGroupsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestGroupsProcessor {
  private GroupsProcessorImpl groupsProcessor;

  @Before
  public void init() {
    groupsProcessor = new GroupsProcessorImpl();
  }

  @Test
  public void testNoProcessorsSet() {
  	Assert.assertTrue(groupsProcessor.getPostProcessorsByType(null).isEmpty());
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
  	Assert.assertEquals(0, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
  	Assert.assertEquals(0, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
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
  	Assert.assertEquals(0, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
  	Assert.assertEquals(0, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
  }

  @Test
  public void injectAnnotationOnPreProcessorField() {
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    groupsProcessor.setUriLocatorFactory(uriLocatorFactory);
    groupsProcessor.addPreProcessor(new ResourcePreProcessor() {
      @Inject
      private UriLocatorFactory factory;
      public void process(final Resource resources, final Reader reader, final Writer writer)
        throws IOException {
        Assert.assertEquals(uriLocatorFactory, factory);
      }
    });
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotUseInjectOnInvalidFieldOfPreProcessor() {
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    groupsProcessor.setUriLocatorFactory(uriLocatorFactory);
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
    groupsProcessor.process(null, null);
  }

  @Test
  public void injectAnnotationOnPostProcessorField() {
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    groupsProcessor.setUriLocatorFactory(uriLocatorFactory);
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
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    groupsProcessor.setUriLocatorFactory(uriLocatorFactory);
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
    final UriLocatorFactory uriLocatorFactory = Mockito.mock(UriLocatorFactory.class);
    groupsProcessor.addPostProcessor(new ResourcePostProcessor() {
      @Inject
      private Object someObject;
      public void process(final Reader reader, final Writer writer)
        throws IOException {
      }
    });
    groupsProcessor.setUriLocatorFactory(uriLocatorFactory);
  }

}
