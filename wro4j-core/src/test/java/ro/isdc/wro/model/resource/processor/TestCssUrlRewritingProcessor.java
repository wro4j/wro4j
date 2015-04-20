/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.support.DefaultResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test for {@link CssUrlRewritingProcessor} class.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestCssUrlRewritingProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(TestCssUrlRewritingProcessor.class);
  private static final Random RANDOM = new Random();
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterConfig filterConfig;

  private CssUrlRewritingProcessor processor;

  private static final String CSS_INPUT_NAME = "cssUrlRewriting.css";

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
    MockitoAnnotations.initMocks(this);
    final Context context = Context.webContext(request, response, filterConfig);
    Context.set(context);
    processor = new CssUrlRewritingProcessor() {
      @Inject
      private ResourceAuthorizationManager authorizationManager;

      @Override
      protected void onProcessCompleted() {
        if (authorizationManager instanceof DefaultResourceAuthorizationManager) {
          LOG.debug("allowed urls: {}", ((DefaultResourceAuthorizationManager) authorizationManager).list());
        }
      }

      @Override
      protected String getUrlPrefix() {
        return "[WRO-PREFIX]?id=";
      }
    };
    WroTestUtils.createInjector().inject(processor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
      throws Exception {
    final URL url = getClass().getResource("cssUrlRewriting");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css",
        (ResourcePreProcessor) processor);
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
   * @param resourceUri
   *          the resource should return.
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
   * Test a servletContext css resource.
   */
  @Test
  public void processServletContextResourceTypeWithNonRootContextPathSet()
      throws IOException {
    when(request.getContextPath()).thenReturn("/myapp");
    WroTestUtils.compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
        "classpath:cssUrlRewriting-servletContext-nonRootContextPath-outcome.css", new ResourcePostProcessor() {
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

  @Test
  public void checkUrlIsAllowed()
      throws Exception {
    processClasspathResourceType();
    assertFalse(processor.isUriAllowed("/WEB-INF/web.xml"));
    assertTrue(processor.isUriAllowed("classpath:folder/img.gif"));
  }

  @Test
  public void shouldSupportOnlyCssResources() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.CSS);
  }

  /**
   * Tests that the Context injected into processor is thread safe and uses the values of set by the thread which runs
   * the processor.
   */
  @Test
  public void shouldUseCorrectAggregatedFolderSetEvenWhenContextIsChangedInAnotherThread()
      throws Exception {
    WroTestUtils.createInjector().inject(processor);
    WroTestUtils.runConcurrently(new Callable<Void>() {
      public Void call()
          throws Exception {
        try {
          Context.set(Context.standaloneContext());
          if (RANDOM.nextBoolean()) {
            processServletContextResourceTypeWithAggregatedFolderSet();
          } else {
            // ensure that a thread uses null aggregatedFolderPath which is injected into processor.
            Context.get().setAggregatedFolderPath(null);
          }
          return null;
        } finally {
          Context.unset();
        }
      }
    }, 20);
  }
}
