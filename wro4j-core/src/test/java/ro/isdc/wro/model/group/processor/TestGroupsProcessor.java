/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * TestGroupsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestGroupsProcessor {
  private GroupsProcessor victim;
  
  @Before
  public void setUp() {
    victim = new GroupsProcessor();
    initVictim(new WroConfiguration());
  }

  private void initVictim(final WroConfiguration config) {
    Context.set(Context.standaloneContext(), config);
    final Injector injector = new InjectorBuilder().build();
    injector.inject(victim);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotProcessNullGroup() {
    victim.process(null, ResourceType.JS, true);
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotProcessNullResourceType() {
    victim.process(new Group("group"), null, true);
  }
  
  @Test
  public void shouldReturnEmptyStringWhenGroupHasNoResources() {
    Assert.assertEquals(StringUtils.EMPTY, victim.process(new Group("group"), ResourceType.JS, true));
  }
  
  /**
   * Same as above, but with ignoreEmptyGroup config updated.
   */
  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenGroupHasNoResources() {
    WroConfiguration config = new WroConfiguration();
    config.setIgnoreEmptyGroup(false);
    initVictim(config);
    Assert.assertEquals(StringUtils.EMPTY, victim.process(new Group("group"), ResourceType.JS, true));
  }

//
//  @Test
//  public void injectAnnotationOnPreProcessorField() {
//    final UriLocatorFactory uriLocatorFactory = groupsProcessor.getUriLocatorFactory();
//    final ResourcePreProcessor processor = new ResourcePreProcessor() {
//      @Inject
//      private SimpleUriLocatorFactory factory;
//      @Inject
//      private PreProcessorExecutor preProcessorExecutor;
//      public void process(final Resource resources, final Reader reader, final Writer writer)
//        throws IOException {
//        Assert.assertEquals(uriLocatorFactory, factory);
//        Assert.assertNotNull(preProcessorExecutor);
//      }
//    };
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPreProcessor(processor));
//  }
//
//  @Test(expected=WroRuntimeException.class)
//  public void cannotUseInjectOnInvalidFieldOfPreProcessor() {
//    final ResourcePreProcessor processor = new ResourcePreProcessor() {
//      @Inject
//      private Object someObject;
//      public void process(final Resource resources, final Reader reader, final Writer writer)
//        throws IOException {
//      }
//    };
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPreProcessor(processor));
//  }
//
//
//  @Test(expected=WroRuntimeException.class)
//  public void cannotUseInjectOnInvalidFieldOfPostProcessor() {
//    final ResourcePostProcessor postProcessor = new ResourcePostProcessor() {
//      @Inject
//      private Object someObject;
//      public void process(final Reader reader, final Writer writer)
//        throws IOException {
//      }
//    };
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPostProcessor(postProcessor));
//  }
//
//  @Test(expected=WroRuntimeException.class)
//  public void cannotAddProcessorBeforeSettingUriLocatorFactory() {
//    final ResourcePostProcessor postProcessor = new ResourcePostProcessor() {
//      @Inject
//      private Object someObject;
//      public void process(final Reader reader, final Writer writer)
//        throws IOException {
//      }
//    };
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPostProcessor(postProcessor));
//  }
//
//  /**
//   * Creates a mocked {@link ResourcePostProcessor} object used to check how many times it was invoked depending on
//   * minimize flag.
//   *
//   * @return {@link ResourcePostProcessor} mock object.
//   */
//  private ResourcePostProcessor getMinimizeAwareProcessorWithMinimizeSetTo(final boolean minimize) {
//    final Group group = new Group();
//    group.setResources(Arrays.asList(Resource.create("classpath:ro/isdc/wro/processor/cssImports/test1-input.css", ResourceType.CSS)));
//    final List<Group> groups = Arrays.asList(group);
//    groupsProcessor = new GroupsProcessor() {
//      @Override
//      protected void configureUriLocatorFactory(final SimpleUriLocatorFactory factory) {
//        factory.addUriLocator(new ClasspathUriLocator());
//      };
//    };
//
//    final ResourcePostProcessor postProcessor = Mockito.mock(JawrCssMinifierProcessor.class);
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPostProcessor(postProcessor));
//    groupsProcessor.process(groups, ResourceType.CSS, minimize);
//    return postProcessor;
//  }
//
//
////  @Test
////  public void testGroupHashCode() {
////    final Group group = new Group();
////    final Resource resource = Resource.create("classpath:ro/isdc/wro/processor/cssImports/test1-input.css", ResourceType.CSS);
////    group.setResources(Arrays.asList(resource));
////    Assert.assertEquals(0, resource.hashCode());
////    Assert.assertEquals(0, group.hashCode());
////  }
//
//
//  /**
//   * Check if minimize aware processor is called when minimization is wanted.
//   * @throws Exception
//   */
//  @Test
//  public void testMinimizeAwareProcessorIsCalled() throws Exception {
//    final ResourcePostProcessor postProcessor = getMinimizeAwareProcessorWithMinimizeSetTo(true);
//    Mockito.verify(postProcessor, Mockito.times(1)).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
//  }
//
//
//  /**
//   * Check if minimize aware processor is not called when minimization is not wanted.
//   * @throws Exception
//   */
//  @Test
//  public void testMinimizeAwareProcessorIsNotCalled() throws Exception {
//    final ResourcePostProcessor postProcessor = getMinimizeAwareProcessorWithMinimizeSetTo(false);
//    Mockito.verify(postProcessor, Mockito.times(0)).process(Mockito.any(Reader.class), Mockito.any(Writer.class));
//  }
//
//  @Test
//  public void injectAnnotationOnPostProcessorField() {
//    final SimpleUriLocatorFactory uriLocatorFactory = groupsProcessor.getUriLocatorFactory();
//    final ResourcePostProcessor postProcessor = new ResourcePostProcessor() {
//      @Inject
//      private SimpleUriLocatorFactory factory;
//      public void process(final Reader reader, final Writer writer)
//        throws IOException {
//        Assert.assertEquals(uriLocatorFactory, factory);
//      }
//    };
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPostProcessor(postProcessor));
//  }
//  @Test
//  public void testGroupWithCssImportProcessor() throws Exception {
//    final Group group = new Group();
//    group.setResources(Arrays.asList(Resource.create("classpath:ro/isdc/wro/processor/cssImports/test1-input.css", ResourceType.CSS)));
//    final List<Group> groups = Arrays.asList(group);
//    groupsProcessor = new GroupsProcessor() {
//      @Override
//      protected void configureUriLocatorFactory(final SimpleUriLocatorFactory factory) {
//        factory.addUriLocator(new ClasspathUriLocator());
//      };
//    };
//
//    final ResourcePreProcessor preProcessor = Mockito.mock(ResourcePreProcessor.class);
//    groupsProcessor.setProcessorsFactory(new SimpleProcessorsFactory().addPreProcessor(preProcessor).addPreProcessor(
//      new CssImportPreProcessor()));
//
//    groupsProcessor.process(groups, ResourceType.CSS, true);
//    Mockito.verify(preProcessor, Mockito.times(6)).process(Mockito.any(Resource.class), Mockito.any(Reader.class), Mockito.any(Writer.class));
//  }

}
