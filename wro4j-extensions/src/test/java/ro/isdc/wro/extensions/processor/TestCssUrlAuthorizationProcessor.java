/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlAuthorizationProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link ro.isdc.wro.model.resource.processor.impl.css.CssUrlAuthorizationProcessor} class, which also test
 * LESS support from wro4j-extensions. See the same class int he wro4j-core for a basic test.
 *
 * @author Greg Pendlebury
 */
public class TestCssUrlAuthorizationProcessor {
  private CssUrlRewritingProcessor rewriter;
  private Less4jProcessor compiler;
  private CssUrlAuthorizationProcessor processor;

  private static final String LESS_INPUT_NAME = "test/lessUrlRewriting.less";
  private static final String LESS_OUTPUT_NAME = "test/lessUrlRewritingOutput.css";
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

    compiler = new Less4jProcessor();
    injector.inject(compiler);

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
  public void testLessCompiler()
      throws Exception {
    final String resourceUri = "classpath:" + LESS_INPUT_NAME;
    WroTestUtils.compareProcessedResourceContents(resourceUri, "classpath:" + LESS_OUTPUT_NAME,
        new ResourcePostProcessor() {
          @Override
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            // Sample URLs that the CSS URL rewriter does not authorize... make sure we still don't:
            final String badUri = "classpath:test/@{iconSpritePath}";
            final String goodUri = "classpath:test/less-test-glyphicons-halflings.png";

            // Run the CSS rewriter with a proxy
            StringWriter writerProxy = new StringWriter();
            rewriter.process(createMockResource(resourceUri), reader, writerProxy);
            String rewrittenOutput = writerProxy.toString();

            // With just the CSS URL rewriter
            assertTrue("Invalid URI is not authorized", processor.isUriAllowed(badUri));
            assertFalse("Valid URI should not have been authorized yet", processor.isUriAllowed(goodUri));
            assertTrue("Invalid URI is not present in content", rewrittenOutput.contains(badUri));
            assertFalse("Valid URI is present in content", rewrittenOutput.contains(goodUri));

            // LESS compiler
            StringReader readerProxy = new StringReader(rewrittenOutput);
            writerProxy = new StringWriter();
            compiler.process(readerProxy, writerProxy);
            rewrittenOutput = writerProxy.toString();

            // With the two combined... the content will now be good, but not the authorization
            assertTrue("Invalid URI is not authorized", processor.isUriAllowed(badUri));
            assertFalse("Valid URI should not have been authorized yet", processor.isUriAllowed(goodUri));
            assertFalse("Invalid URI is present in content", rewrittenOutput.contains(badUri));
            assertTrue("Valid URI is not present in content", rewrittenOutput.contains(goodUri));

            // So we run the authorization processor to fix that
            readerProxy = new StringReader(rewrittenOutput);
            processor.process(readerProxy, writer);
            assertEquals("Authorization processor should never alter content", rewrittenOutput, writer.toString());
            assertTrue("Invalid URI is not authorized", processor.isUriAllowed(badUri));
            assertTrue("Valid URI is not authorized", processor.isUriAllowed(goodUri));
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