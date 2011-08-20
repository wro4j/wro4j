/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link CssUrlRewritingProcessor} class.
 * <p/>
 * TODO improve unit tests.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestCssUrlRewritingProcessor {
  private CssUrlRewritingProcessor processor;

  private static final String CSS_INPUT_NAME = "cssUrlRewriting.css";


  @Before
  public void init() {
    Context.set(Context.standaloneContext());
    processor = new CssUrlRewritingProcessor() {
      @Override
      protected String getUrlPrefix() {
        return "[WRO-PREFIX]?id=";
      }
    };
  }


  /**
   * When background url contains a dataUri, the rewriting should have no effect.
   */
  @Test
  public void processResourceWithDataUriEncodedValue()
    throws IOException {
    final String resourceUri = "classpath:cssUrlRewriting-dataUri.css";
    WroTestUtils.compareProcessedResourceContents(resourceUri, resourceUri, new ResourcePostProcessor() {
      public void process(final Reader reader, final Writer writer)
        throws IOException {
        processor.process(createMockResource(resourceUri), reader, writer);
      }
    });
  }


  /**
   * Test a classpath css resource.
   */
  @Test
  public void processClasspathResourceType()
    throws IOException {
    final String resourceUri = "classpath:" + CSS_INPUT_NAME;
    WroTestUtils.compareProcessedResourceContents(resourceUri, "classpath:cssUrlRewriting-classpath-outcome.css",
      new ResourcePostProcessor() {
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
    WroTestUtils.compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
      "classpath:cssUrlRewriting-servletContext-outcome.css", new ResourcePostProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource("/static/img/" + CSS_INPUT_NAME), reader, writer);
        }
      });
  }


  /**
   * Test a servletContext css resource.
   */
  @Test
  public void processServletContextResourceTypeWithAggregatedFolderSet()
    throws IOException {
    Context.get().setAggregatedFolderPath("wro/css");
    WroTestUtils.compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
      "classpath:cssUrlRewriting-servletContext-aggregatedFolderSet-outcome.css", new ResourcePostProcessor() {
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
    WroTestUtils.compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
      "classpath:cssUrlRewriting-WEBINFservletContext-outcome.css", new ResourcePostProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource("/WEB-INF/" + CSS_INPUT_NAME), reader, writer);
        }
      });
  }


  /**
   * Test a url css resource.
   */
  @Test
  public void processUrlResourceType()
    throws IOException {
    WroTestUtils.compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
      "classpath:cssUrlRewriting-url-outcome.css", new ResourcePostProcessor() {
        public void process(final Reader reader, final Writer writer)
          throws IOException {
          processor.process(createMockResource("http://www.site.com/static/css/" + CSS_INPUT_NAME), reader, writer);
        }
      });
  }
}
