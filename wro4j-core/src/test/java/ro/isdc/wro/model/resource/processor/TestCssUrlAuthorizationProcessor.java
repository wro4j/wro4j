/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlAuthorizationProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link ro.isdc.wro.model.resource.processor.impl.css.CssUrlAuthorizationProcessor} class.
 *
 * @author Greg Pendlebury
 * @created Created on June 10, 2014 Adapted from
 *          {@link ro.isdc.wro.model.resource.processor.TestCssUrlRewritingProcessor}
 * @author Alex Objelean
 */
public class TestCssUrlAuthorizationProcessor {
  private CssUrlRewritingProcessor rewriter;
  private CssUrlAuthorizationProcessor processor;

  private static final String CSS_INPUT_NAME = "cssUrlRewriting.css";
  private static final String WRO_URL_PREFIX = "[WRO-PREFIX]?id=";

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    final Injector injector = WroTestUtils.createInjector();

    rewriter = new CssUrlRewritingProcessor() {
      @Override
      protected String getUrlPrefix() {
        return WRO_URL_PREFIX;
      }
    };
    injector.inject(rewriter);

    processor = new CssUrlAuthorizationProcessor() {
      @Override
      protected String getUrlPrefix() {
        return WRO_URL_PREFIX;
      }
    };
    injector.inject(processor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testBasics()
      throws Exception {
    final String resourceUri = "classpath:" + CSS_INPUT_NAME;
    WroTestUtils.compareProcessedResourceContents(resourceUri, "classpath:cssUrlRewriting-classpath-outcome.css",
        new ResourcePostProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            // Sample URLs that the CSS URL rewriter does not authorize... make sure we still don't:
            final String testUrl = "http://www.google.com/";

            // Run the CSS rewriter with a proxy
            final StringWriter writerProxy = new StringWriter();
            rewriter.process(createMockResource(resourceUri), reader, writerProxy);
            final String rewrittenOutput = writerProxy.toString();
            assertFalse("Unexpected URL is already authorized", processor.isUriAllowed(testUrl));

            // Then run the authorization processor
            final StringReader readerProxy = new StringReader(rewrittenOutput);
            processor.process(readerProxy, writer);
            assertEquals("Authorization processor should never alter content", rewrittenOutput,
                writer.toString());
            assertFalse("URL should not be authorized", processor.isUriAllowed(testUrl));
          }
        });
  }

  /**
   * @param resourceUri
   *          the resource should return.
   * @return mocked {@link ro.isdc.wro.model.resource.Resource} object.
   */
  private Resource createMockResource(final String resourceUri) {
    final Resource resource = Mockito.mock(Resource.class);
    Mockito.when(resource.getUri()).thenReturn(resourceUri);
    return resource;
  }
}