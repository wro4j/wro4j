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
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.factory.UriLocatorFactoryImpl;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.CssEmbedPreProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;


/**
 * TestCssEmbedPreProcessor.
 *
 * @author Alex Objelean
 * @created Created on Mat 09, 2010
 */
public class TestCssEmbedPreProcessor extends AbstractWroTest {
  private ResourcePreProcessor processor;
  private static final String TEST_FOLDER = "ro/isdc/wro/processor/dataUri/";
  private static final String CSS_INPUT_NAME = TEST_FOLDER + "cssEmbed-input.css";


  @Before
  public void init() {
    processor = new CssEmbedPreProcessor();

    final UriLocatorFactoryImpl uriLocatorFactory = new UriLocatorFactoryImpl();
    uriLocatorFactory.addUriLocator(new ServletContextUriLocator());
    uriLocatorFactory.addUriLocator(new UrlUriLocator());
    uriLocatorFactory.addUriLocator(new ClasspathUriLocator());

    final GroupsProcessorImpl groupsProcessor = new GroupsProcessorImpl();
    groupsProcessor.setUriLocatorFactory(uriLocatorFactory);
    groupsProcessor.addPreProcessor(processor);
  }


  /**
   * Test a classpath css resource.
   *
   * @throws IOException
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
   * @param resourceUri the resource should return.
   * @return mocked {@link Resource} object.
   */
  private Resource createMockResource(final String resourceUri) {
    final Resource resource = Mockito.mock(Resource.class);
    Mockito.when(resource.getUri()).thenReturn(resourceUri);
    return resource;
  }


  /**
   * Test a servletContext css resource.
   */
  @Test
  public void processServletContextResourceType()
    throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
      "classpath:cssUrlRewriting-servletContext-outcome.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource("/static/img/" + CSS_INPUT_NAME), reader, writer);
        }
      });
  }

  /**
   * Test a resource which is located inside WEB-INF protected folder.
   */
  @Test
  public void processWEBINFServletContextResourceType()
    throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
      "classpath:cssUrlRewriting-WEBINFservletContext-outcome.css", new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource("/WEB-INF/" + CSS_INPUT_NAME), reader, writer);
        }
      });
  }

  /**
   * Test a url css resource.
   *
   * @throws IOException
   */
  @Test
  public void processUrlResourceType()
    throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME, "classpath:cssUrlRewriting-url-outcome.css",
      new ResourceProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource("http://www.site.com/static/css/" + CSS_INPUT_NAME), reader, writer);
        }
      });
  }
}
