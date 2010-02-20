/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactoryImpl;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssImportPreProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;


/**
 * Test for css import processor.
 *
 * @author Alex Objelean
 */
public class TestCssImportPreProcessor extends AbstractWroTest {
  private final CssImportPreProcessor processor = new CssImportPreProcessor();


  @Test
  public void testPreProcessorWithoutRecursion1()
    throws IOException {
    genericTest("classpath:ro/isdc/wro/processor/cssImports/test1-input.css",
      "classpath:ro/isdc/wro/processor/cssImports/test1-output.css");
  }


  @Test
  public void testPreProcessorWithoutRecursion2()
    throws IOException {
    genericTest("classpath:ro/isdc/wro/processor/cssImports/test2-input.css",
      "classpath:ro/isdc/wro/processor/cssImports/test2-output.css");
  }


  /**
   * Checks a situation when the css contains an import to itself.
   *
   * @throws IOException
   */
  @Test
  public void testPreProcessorWithImmediateRecursivity()
    throws IOException {
    genericTest("classpath:ro/isdc/wro/processor/cssImports/testRecursive-input.css",
      "classpath:ro/isdc/wro/processor/cssImports/testRecursive-output.css");
  }


  /**
   * Level 2 recursivity test. When a referred css contain an import to original css.
   */
  @Test
  public void testPreProcessorWithDeepRecursivity()
    throws IOException {
    genericTest("classpath:ro/isdc/wro/processor/cssImports/testRecursive1-input.css",
      "classpath:ro/isdc/wro/processor/cssImports/testRecursive1-output.css");
  }

  @Test
  public void testPreProcessorWithBackgrounds() throws IOException {
    genericTest("classpath:ro/isdc/wro/processor/cssImports/testPostProcessorWithBackgrounds-input.css",
    "classpath:ro/isdc/wro/processor/cssImports/testPostProcessorWithBackgrounds-output.css");
  }


  /**
   * @param inputUri the uri of the input css to process.
   * @param outputUri the uri of the output css containing the expected processed content.
   * @param groupResourceUris an array of expected uri's contained inside the Group after processing.
   * @throws IOException
   */
  private void genericTest(final String inputUri, final String outputUri)
    throws IOException {
    // this is necessary use GroupsProcessor instrumentation on added processor
    updateGroupsProcessorDependencies(processor);
    final Resource resource = createResource(inputUri);
    compareProcessedResourceContents(inputUri, outputUri, new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(resource, reader, writer);
      }
    });
  }


  /**
   * Create a resource and add associate it with a group.
   */
  private Resource createResource(final String uri) {
    final Group group = new Group();
    final Resource resource = Resource.create(uri, ResourceType.CSS);
    resource.setGroup(group);
    group.setResources(Arrays.asList(new Resource[] { resource }));
    return resource;
  }


  /**
   * This method will allow the fields containing @Inject annotations to be assigned.
   */
  private void updateGroupsProcessorDependencies(final ResourcePreProcessor processor) {
    final GroupsProcessor groupsProcessor = new GroupsProcessorImpl();
    groupsProcessor.setUriLocatorFactory(getUriLocatorFactory());
    groupsProcessor.addPreProcessor(processor);
  }


  /**
   * @return prepared {@link UriLocatorFactory} with few uriLocators set.
   */
  private UriLocatorFactory getUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
    factory.addUriLocator(new ServletContextUriLocator());
    return factory;
  }
}
