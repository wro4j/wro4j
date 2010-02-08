/*
 * Copyright (c) 2009. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.processor.impl.CssImportProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;
import ro.isdc.wro.test.util.ResourceProcessor;


/**
 * Test for css import processor.
 *
 * @author Alex Objelean
 */
public class TestCssImportProcessor extends AbstractWroTest {
  private final CssImportProcessor processor = new CssImportProcessor();


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
      "classpath:ro/isdc/wro/processor/cssImports/testRecursive-input.css");
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
  public void testPostProcessor() throws IOException {
    compareProcessedResourceContents("classpath:ro/isdc/wro/processor/cssImports/testPostProcessor-input.css", "classpath:ro/isdc/wro/processor/cssImports/testPostProcessor-output.css", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(reader, writer);
      }
    });
  }


  @Test
  public void testPreProcessorWithBackgrounds() throws IOException {
    compareProcessedResourceContents("classpath:ro/isdc/wro/processor/cssImports/testPostProcessorWithBackgrounds-input.css", "classpath:ro/isdc/wro/processor/cssImports/testPostProcessorWithBackgrounds-output.css", new ResourceProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(reader, writer);
      }
    });
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
