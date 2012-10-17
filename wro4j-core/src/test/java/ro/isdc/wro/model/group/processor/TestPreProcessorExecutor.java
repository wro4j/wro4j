/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.group.processor;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.factory.AbstractResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.AbstractResourceLocator;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestPreProcessorExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(TestPreProcessorExecutor.class);
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private ResourceLocatorFactory mockLocatorFactory;
  @Mock
  private ResourceLocator mockLocator;
  private PreProcessorExecutor victim;

  @Before
  public void setUp()
      throws Exception {
    initMocks(this);

    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(""));
    when(mockRequest.getServletPath()).thenReturn("");
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockLocatorFactory.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);
    when(mockLocator.getInputStream()).thenReturn(WroUtil.EMPTY_STREAM);
    when(mockLocatorFactory.getLocator(Mockito.anyString())).thenReturn(mockLocator);

    final Context context = Context.webContext(mockRequest, mockResponse, mockFilterConfig);
    Context.set(context);
    // force parallel execution
    Context.get().getConfig().setParallelPreprocessing(true);
    Context.get().getConfig().setIgnoreFailingProcessor(true);
    initExecutor();
  }

  private WroManagerFactory createWroManager(final ResourceProcessor... preProcessors) {
    final SimpleProcessorsFactory processorsFactory = new SimpleProcessorsFactory();
    for (final ResourceProcessor resourcePreProcessor : preProcessors) {
      processorsFactory.addPreProcessor(resourcePreProcessor);
    }
    final BaseWroManagerFactory wroManagerFactory = new BaseWroManagerFactory();
    wroManagerFactory.setProcessorsFactory(processorsFactory);
    wroManagerFactory.setLocatorFactory(mockLocatorFactory);
    return wroManagerFactory;
  }

  /**
   * @param wroManagerFactory
   */
  private void initExecutor(final ResourceProcessor... preProcessors) {
    final WroManagerFactory wroManagerFactory = createWroManager(preProcessors);
    final Injector injector = InjectorBuilder.create(wroManagerFactory).build();
    victim = new PreProcessorExecutor();
    injector.inject(victim);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArguments()
      throws Exception {
    victim.processAndMerge(null, true);
  }

  /**
   * Creates a slow pre processor which sleeps for a given amount of milliseconds and doesn't change the processed
   * content.
   */
  private ResourceProcessor createSlowPreProcessor(final long time) {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        try {
          IOUtils.copy(reader, writer);
          Thread.sleep(time);
        } catch (final InterruptedException e) {
        }
      }
    };
  }

  private ResourceProcessor createProcessorUsingMissingResource() {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        LOG.debug("executing processor which will throw IOException");
        throw new IOException("Invalid resource found!");
      }
    };
  }

  private ResourceProcessor createProcessorWhichFails() {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        LOG.debug("executing failing processor...");
        throw new WroRuntimeException("Boom!");
      }
    };
  }

  @Test
  public void processEmptyList()
      throws Exception {
    final List<Resource> resources = new ArrayList<Resource>();
    final Group group = new Group("dummy");
    group.setResources(resources);
    Assert.assertEquals("", victim.processAndMerge(group, true));
    Assert.assertEquals("", victim.processAndMerge(group, false));
  }

  @Test
  public void shouldNotFailWhenNoResourcesProcessed()
      throws Exception {
    initExecutor(createProcessorUsingMissingResource());
    victim.processAndMerge(createGroup(), true);
  }

  private Group createGroup(final Resource... resources) {
    final Group group = new Group("dummy");
    final List<Resource> resourcesList = new ArrayList<Resource>();
    for (final Resource resource : resources) {
      resourcesList.add(resource);
    }
    group.setResources(resourcesList);
    return group;
  }

  @Test(expected = IOException.class)
  public void shouldFailWhenProcessingInvalidResource()
      throws Exception {
    when(mockLocator.getInputStream()).thenThrow(IOException.class);
    Context.get().getConfig().setIgnoreMissingResources(false);
    shouldNotFailWhenProcessingInvalidResource();
  }

  @Test
  public void shouldNotFailWhenProcessingInvalidResource()
      throws IOException {
    initExecutor(createProcessorUsingMissingResource());
    final Group group = createGroup(Resource.create("/uri", ResourceType.JS));
    final String result = victim.processAndMerge(group, true);
    Assert.assertEquals("", result);
  }

  @Test(expected = WroRuntimeException.class)
  public void shouldFailWhenUsingFailingPreProcessor()
      throws Exception {
    Context.get().getConfig().setIgnoreFailingProcessor(false);
    useFailingPreProcessor();
  }

  @Test
  public void shouldNotFailWhenUsingFailingPreProcessor()
      throws Exception {
    Context.get().getConfig().setIgnoreFailingProcessor(true);
    useFailingPreProcessor();
  }

  private void useFailingPreProcessor()
      throws Exception {
    initExecutor(createProcessorWhichFails());
    final Group group = createGroup(Resource.create("", ResourceType.JS));
    final String result = victim.processAndMerge(group, true);
    Assert.assertEquals("", result);

  }

  /**
   * This test should work when running at least on dual-core. It assumes that (P1(r1) + P2(r1) + P3(r1)) + (P1(r2) +
   * P2(r2) + P3(r2)) > Parallel(P1(r1) + P2(r1) + P3(r1) | P1(r2) + P2(r2) + P3(r2))
   */
  @Test
  public void preProcessingInParallelIsFaster()
      throws Exception {
    final int availableProcessors = Runtime.getRuntime().availableProcessors();
    LOG.info("availableProcessors: {}", availableProcessors);
    // test it only if number there are more than 1 CPU cores are available
    if (availableProcessors > 1) {
      final StopWatch watch = new StopWatch();
      final WroConfiguration config = Context.get().getConfig();

      initExecutor(createSlowPreProcessor(100), createSlowPreProcessor(100), createSlowPreProcessor(100));
      final Group group = createGroup(Resource.create("r1", ResourceType.JS), Resource.create("r2", ResourceType.JS));

      // warm up
      config.setParallelPreprocessing(true);
      victim.processAndMerge(group, true);

      // parallel
      watch.start("parallel preProcessing");
      config.setParallelPreprocessing(true);
      victim.processAndMerge(group, true);
      watch.stop();
      final long parallelExecution = watch.getLastTaskTimeMillis();

      // sequential
      config.setParallelPreprocessing(false);
      watch.start("sequential preProcessing");
      victim.processAndMerge(group, true);
      watch.stop();
      final long sequentialExecution = watch.getLastTaskTimeMillis();

      final String message = "Processing details: \n" + watch.prettyPrint();
      LOG.debug(message);

      // prove that running in parallel is faster
      // delta indicates the improvement relative to parallel execution (we use 80% relative improvement, but it
      // normally
      // should be about 100%).
      final double delta = parallelExecution * 0.8;
      Assert.assertTrue(String.format("%s  > %s + %s", sequentialExecution, parallelExecution, delta),
          sequentialExecution > parallelExecution + delta);
    }
  }

  @Test
  public void shouldNotMinimizeDecoratedResourcesWithMinimizationDisabled()
      throws Exception {
    final Resource resource = Resource.create("classpath:1.js");
    resource.setMinimize(false);
    final ResourceProcessor preProcessor = CopyrightKeeperProcessorDecorator.decorate(new JSMinProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        Assert.fail("Should not minimize");
      }
    });
    initExecutor(preProcessor);
    final Group group = new Group("group").addResource(resource);
    victim.processAndMerge(group, true);
  }

  /**
   * When an empty resource is processed, the processing should not fail (warn only).
   */
  @Test
  public void shouldNotFailWhenEmptyResourceIsFound()
      throws Exception {
    final WroConfiguration config = Context.get().getConfig();
    config.setIgnoreMissingResources(false);

    final ResourceLocator emptyStreamLocator = new AbstractResourceLocator() {
      public InputStream getInputStream()
          throws IOException {
        return new ByteArrayInputStream("".getBytes());
      }
    };
    final ResourceLocatorFactory locatorFactory = new AbstractResourceLocatorFactory() {
      public ResourceLocator getLocator(final String uri) {
        return emptyStreamLocator;
      }
    };
    // init executor
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setLocatorFactory(locatorFactory);
    InjectorBuilder.create(managerFactory).build().inject(victim);

    final List<Resource> resources = new ArrayList<Resource>();
    resources.add(Resource.create("/resource.js"));

    final Group group = new Group("name");
    group.setResources(resources);
    victim.processAndMerge(group, true);
  }


  private static class AnyTypeProcessor
      implements ResourceProcessor {
    public void process(final Resource resource, final Reader reader, final Writer writer)
        throws IOException {
    }
  }

  @Minimize
  private static class MinimizeAwareProcessor
      extends AnyTypeProcessor {
  }


  @After
  public void tearDown() {
    Context.unset();
  }
}
