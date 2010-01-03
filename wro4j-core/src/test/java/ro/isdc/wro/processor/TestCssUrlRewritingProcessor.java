/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * TestProcessor.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @created Created on Nov 3, 2008
 */
public class TestCssUrlRewritingProcessor extends AbstractWroTest {
  private ResourcePreProcessor processor;

  private static final String CSS_INPUT_NAME = "cssUrlRewriting.css";

  @Before
  public void init() {
    processor = new CssUrlRewritingProcessor() {
      @Override
      protected String getUrlPrefix() {
        return "[WRO-PREFIX]?id=";
      }
    };
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
    compareProcessedResourceContents(resourceUri, "classpath:cssUrlRewriting-classpath-outcome.css",
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
   *
   * @throws IOException
   */
  @Test
  public void processServletContextResourceType() throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
        "classpath:cssUrlRewriting-servletContext-outcome.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(createMockResource("/static/img/" + CSS_INPUT_NAME), reader, writer);
          }
        });
  }

  /**
   * Test a url css resource.
   *
   * @throws IOException
   */
  @Test
  public void processUrlResourceType() throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
        "classpath:cssUrlRewriting-url-outcome.css", new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process(createMockResource("http://www.site.com/static/css/"
                + CSS_INPUT_NAME), reader, writer);
          }
        });
  }
}
