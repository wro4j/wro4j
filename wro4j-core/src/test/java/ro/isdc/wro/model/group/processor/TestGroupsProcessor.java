/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import org.junit.Test;


/**
 * TestGroupsProcessor.
 *
 * @author Alex Objelean
 * @created Created on Jan 5, 2010
 */
public class TestGroupsProcessor {
  @Test
  public void justPass() {}
//  private GroupsProcessor groupsProcessor;
//
//  @Before
//  public void init() {
//    groupsProcessor = new GroupsProcessor();
//  }
//
////  @Test
////  public void testNoProcessorsSet() {
////  	Assert.assertTrue(groupsProcessor.getPostProcessorsByType(null).isEmpty());
////  }
////
////  /**
////   * Test if getPostProcessorsByType is implemented correctly
////   */
////  @Test
////  public void testMixedPreProcessors() {
////    final Collection<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
////    processors.add(new CssMinProcessor());
////    processors.add(new BomStripperPreProcessor());
////    groupsProcessor.setResourcePreProcessors(processors);
////    Assert.assertEquals(1, groupsProcessor.getPreProcessorsByType(null).size());
////    Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
////    Assert.assertEquals(1, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
////  }
////
////  /**
////   * Test if getPostProcessorsByType is implemented correctly
////   */
////  @Test
////  public void testMixedPostProcessors() {
////    final Collection<ResourcePostProcessor> processors = new ArrayList<ResourcePostProcessor>();
////    processors.add(new CssMinProcessor());
////    processors.add(new CommentStripperProcessor());
////    groupsProcessor.setResourcePostProcessors(processors);
////    Assert.assertEquals(1, groupsProcessor.getPostProcessorsByType(null).size());
////    Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
////    Assert.assertEquals(1, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
////  }
////
////  /**
////   * Test if getPostProcessorsByType is implemented correctly
////   */
////  @Test
////  public void testGetPostProcessorsByType1() {
////  	final Collection<ResourcePostProcessor> processors = new ArrayList<ResourcePostProcessor>();
////  	processors.add(new CssMinProcessor());
////  	processors.add(new JSMinProcessor());
////  	processors.add(new CssVariablesProcessor());
////  	groupsProcessor.setResourcePostProcessors(processors);
////  	Assert.assertEquals(0, groupsProcessor.getPostProcessorsByType(null).size());
////  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
////  	Assert.assertEquals(1, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
////  }
////
////  @Test
////  public void testGetPostProcessorsByNullType2() {
////  	final Collection<ResourcePostProcessor> processors = new ArrayList<ResourcePostProcessor>();
////  	processors.add(new MultiLineCommentStripperProcessor());
////  	processors.add(new SingleLineCommentStripperProcessor());
////  	groupsProcessor.setResourcePostProcessors(processors);
////  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(null).size());
////  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.CSS).size());
////  	Assert.assertEquals(2, groupsProcessor.getPostProcessorsByType(ResourceType.JS).size());
////  }
////
////  @Test
////  public void testGetPreProcessorsByNullType1() {
////  	final Collection<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
////  	processors.add(new CssMinProcessor());
////  	processors.add(new JSMinProcessor());
////  	processors.add(new CssVariablesProcessor());
////  	groupsProcessor.setResourcePreProcessors(processors);
////  	Assert.assertEquals(0, groupsProcessor.getPreProcessorsByType(null).size());
////  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
////  	Assert.assertEquals(1, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
////  }
////
////  @Test
////  public void testGetPreProcessorsByNullType2() {
////  	final Collection<ResourcePreProcessor> processors = new ArrayList<ResourcePreProcessor>();
////  	processors.add(new MultiLineCommentStripperProcessor());
////  	processors.add(new SingleLineCommentStripperProcessor());
////  	groupsProcessor.setResourcePreProcessors(processors);
////  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(null).size());
////  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.CSS).size());
////  	Assert.assertEquals(2, groupsProcessor.getPreProcessorsByType(ResourceType.JS).size());
////  }
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
//  @Test(expected=IllegalArgumentException.class)
//  public void cannotAcceptNullArguments() {
//    groupsProcessor.process(null, null, true);
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
