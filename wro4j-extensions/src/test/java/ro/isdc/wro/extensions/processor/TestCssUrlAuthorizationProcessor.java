/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
 * Test for {@link ro.isdc.wro.model.resource.processor.impl.css.CssUrlAuthorizationProcessor} class, which also
 * test LESS support from wro4j-extensions. See the same class int he wro4j-core for a basic test.
 *
 * @author Greg Pendlebury
 * @created Created on June 10, 2014
 * 
 * Adapted from ro.isdc.wro.model.resource.processor.TestCssUrlRewritingProcessor in wro4j-core
 * @author Alex Objelean
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
    Injector injector = WroTestUtils.createInjector();

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
  public void testLessCompiler() throws Exception {
    final String resourceUri = "classpath:" + LESS_INPUT_NAME;
    WroTestUtils.compareProcessedResourceContents(resourceUri, "classpath:" + LESS_OUTPUT_NAME,
            new ResourcePostProcessor() {
              public void process(final Reader reader, final Writer writer)
                      throws IOException {
                // Sample URLs that the CSS URL rewriter does not authorize... make sure we still don't:
                String badUri = "classpath:test/@{iconSpritePath}";
                String goodUri = "classpath:test/less-test-glyphicons-halflings.png";

                // Run the CSS rewriter with a proxy
                StringWriter writerProxy = new StringWriter();
                rewriter.process(createMockResource(resourceUri), reader, writerProxy);
                String rewrittenOutput = writerProxy.toString();

                // With just the CSS URL rewriter
                Assert.assertTrue("Invalid URI is not authorized", processor.isUriAllowed(badUri));
                Assert.assertFalse("Valid URI should not have been authorized yet", processor.isUriAllowed(goodUri));
                Assert.assertTrue("Invalid URI is not present in content", rewrittenOutput.contains(badUri));
                Assert.assertFalse("Valid URI is present in content", rewrittenOutput.contains(goodUri));

                // LESS compiler
                StringReader readerProxy = new StringReader(rewrittenOutput);
                writerProxy = new StringWriter();
                compiler.process(readerProxy, writerProxy);
                rewrittenOutput = writerProxy.toString();

                // With the two combined... the content will now be good, but not the authorization
                Assert.assertTrue("Invalid URI is not authorized", processor.isUriAllowed(badUri));
                Assert.assertFalse("Valid URI should not have been authorized yet", processor.isUriAllowed(goodUri));
                Assert.assertFalse("Invalid URI is present in content", rewrittenOutput.contains(badUri));
                Assert.assertTrue("Valid URI is not present in content", rewrittenOutput.contains(goodUri));

                // So we run the authorization processor to fix that
                readerProxy = new StringReader(rewrittenOutput);
                processor.process(readerProxy, writer);
                Assert.assertEquals("Authorization processor should never alter content", rewrittenOutput, writer.toString());
                Assert.assertTrue("Invalid URI is not authorized", processor.isUriAllowed(badUri));
                Assert.assertTrue("Valid URI is not authorized", processor.isUriAllowed(goodUri));
              }
            });
  }

  /**
   * @param resourceUri the resource should return.
   * @return mocked {@link ro.isdc.wro.model.resource.Resource} object.
   */
  private Resource createMockResource(final String resourceUri) {
    final Resource resource = Mockito.mock(Resource.class);
    Mockito.when(resource.getUri()).thenReturn(resourceUri);
    return resource;
  }
}