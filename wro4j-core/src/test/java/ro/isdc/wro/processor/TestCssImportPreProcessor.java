/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.processor.impl.CssImportPreProcessor;
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
 * Test for css variables preprocessor.
 *
 * @author Alex Objelean
 * @created Created on Jul 05, 2009
 */
public class TestCssImportPreProcessor extends AbstractWroTest {
  private final CssImportPreProcessor processor = new CssImportPreProcessor();

  @Test
  public void testValid() throws IOException {
    //this is necessary to initialize processors using GroupsProcessor instrumentation
    buildGroupsProcessor();
    final String URI = "classpath:ro/isdc/wro/processor/cssImports/test1-input.css";
    final Resource resource = Resource.create(URI, ResourceType.CSS);
    System.out.println("processor.uriLocatorFactory: " + processor.uriLocatorFactory);
    System.out.println("comparing resources");
    compareProcessedResourceContents(URI, "classpath:ro/isdc/wro/processor/cssImports/test1-output.css",
      new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(resource, reader, writer);
          }
        });
  }

  /**
   * Builds a {@link GroupsProcessor} object with all dependencies set.
   */
  private GroupsProcessor buildGroupsProcessor() {
    final GroupsProcessor groupsProcessor = new GroupsProcessorImpl();
    groupsProcessor.setUriLocatorFactory(getUriLocatorFactory());
    groupsProcessor.addPreProcessor(processor);
    return groupsProcessor;
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
