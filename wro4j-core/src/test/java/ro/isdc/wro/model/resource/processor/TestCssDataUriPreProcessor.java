/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;


/**
 * Test for {@link CssDataUriPreProcessor} class.
 *
 * @author Alex Objelean
 * @created Created on Mat 09, 2010
 */
public class TestCssDataUriPreProcessor extends AbstractWroTest {
  private ResourcePreProcessor processor;
  private static final String TEST_FOLDER = "ro/isdc/wro/processor/dataUri/";
  private static final String CSS_INPUT_NAME = TEST_FOLDER + "cssEmbed-input.css";


  @Before
  public void init() {
    processor = new CssDataUriPreProcessor();
    final GroupsProcessor groupsProcessor = new GroupsProcessor() {
      @Override
      protected void configureUriLocatorFactory(final UriLocatorFactory factory) {
        factory.addUriLocator(new ServletContextUriLocator());
        factory.addUriLocator(new UrlUriLocator());
        factory.addUriLocator(new ClasspathUriLocator());
      }
    };

    groupsProcessor.addPreProcessor(processor);
  }


  /**
   * Test a classpath css resource.
   */
  @Test
  public void processClasspathResourceType()
    throws IOException {
    final String resourceUri = "classpath:" + CSS_INPUT_NAME;
    compareProcessedResourceContents(resourceUri, "classpath:" + TEST_FOLDER + "cssEmbed-classpath-outcome.css",
      new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource(resourceUri), reader, writer);
        }
      });
  }

  /**
   * Check if a large dataUri with more than 32KB does not replace original url.
   */
  @Test
  public void processLargeDataUri()
    throws IOException {
    final String resourceUri = "classpath:" + TEST_FOLDER + "cssEmbed-large-input.css";
    compareProcessedResourceContents(resourceUri, resourceUri,
      new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource(resourceUri), reader, writer);
        }
      });
  }


  /**
   * @param resourceUri the resource should return.
   * @return mocked {@link Resource} object.
   */
  private Resource createMockResource(final String resourceUri) {
    final Resource resource = Mockito.mock(Resource.class);
    Mockito.when(resource.getUri()).thenReturn(resourceUri);
    return resource;
  }
}
