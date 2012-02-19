/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.http.HttpHeader;
import ro.isdc.wro.http.UnauthorizedRequestException;
import ro.isdc.wro.manager.callback.PerformanceLoggerCallback;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.NoProcessorsWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactoryDecorator;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.DefaultGroupExtractor;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.util.CRC32HashBuilder;
import ro.isdc.wro.model.resource.util.MD5HashBuilder;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;


/**
 * TestWroManager.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestWroManager {
  private static final Logger LOG = LoggerFactory.getLogger(TestWroManager.class);

  /**
   * A processor which which uses a {@link WroManager} during processor, in order to process a single group, which
   * resource is the pre processed resource of this processor.
   */
  private static final class WroManagerProcessor
    implements ResourcePreProcessor {
    private final WroManagerFactory managerFactory = new BaseWroManagerFactory() {
      @Override
      protected void onAfterInitializeManager(final WroManager manager) {
        manager.registerCallback(new PerformanceLoggerCallback());
      };
    };


    public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
      LOG.debug("resource: {}", resource);

      final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      final HttpServletResponse response = Context.get().getResponse();

      Mockito.when(response.getOutputStream()).thenReturn(
        new DelegatingServletOutputStream(new WriterOutputStream(writer)));
      Mockito.when(request.getRequestURI()).thenReturn("");

      final WroConfiguration config = new WroConfiguration();
      // we don't need caching here, otherwise we'll have clashing during unit tests.
      config.setDebug(true);
      config.setDisableCache(true);
      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);

      // create a groupExtractor which always return the same group name.
      final String groupName = "group";
      final GroupExtractor groupExtractor = new DefaultGroupExtractor() {
        @Override
        public String getGroupName(final HttpServletRequest request) {
          return groupName;
        }


        @Override
        public ResourceType getResourceType(final HttpServletRequest request) {
          return resource.getType();
        }
      };
      // this manager will make sure that we always process a model holding one group which has only one resource.
      final WroManager manager = managerFactory.create();
      manager.setModelFactory(new WroModelFactoryDecorator(getValidModelFactory()) {
        @Override
        public WroModel create() {

          final Group group = new Group("group");
          group.setResources(Arrays.asList(new Resource[] { resource }));
          final WroModel model = super.create();
          model.setGroups(Arrays.asList(group));
          return model;
        }
      });
      manager.setGroupExtractor(groupExtractor);
      manager.process();
    }
  }

  private WroManagerFactory managerFactory;


  @Before
  public void setUp() {
    final Context context = Context.webContext(Mockito.mock(HttpServletRequest.class),
      Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS), Mockito.mock(FilterConfig.class));
    Context.set(context, newConfigWithUpdatePeriodValue(0));
    managerFactory = new BaseWroManagerFactory().setModelFactory(getValidModelFactory());
  }

  private class GenericTestBuilder {
    /**
     * Perform a processing on a group extracted from requestUri and compares with the expectedResourceUri content.
     *
     * @param requestUri contains the group name to process.
     * @param expectedResourceUri the uri of the resource which has the expected content.
     */
    public void processAndCompare(final String requestUri, final String expectedResourceUri)
      throws Exception {
      final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      final HttpServletResponse response = Context.get().getResponse();

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));
      Mockito.when(request.getRequestURI()).thenReturn(requestUri);

      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));

      onBeforeProcess();

      managerFactory.create().process();

      // compare written bytes to output stream with the content from specified css.
      final InputStream expectedInputStream = new UnclosableBufferedInputStream(
        WroTestUtils.getInputStream(expectedResourceUri));
      final InputStream actualInputStream = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
      expectedInputStream.reset();
      WroTestUtils.compare(expectedInputStream, actualInputStream);
      expectedInputStream.close();
      actualInputStream.close();
    }


    /**
     * Allow to execute custom logic before the actual processing is done.
     */
    protected void onBeforeProcess() {}
  }


  /**
   * @return a {@link XmlModelFactory} pointing to a valid config resource.
   */
  private static XmlModelFactory getValidModelFactory() {
    return new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        return TestWroManager.class.getResourceAsStream("wro.xml");
      }
    };
  }

  @Test(expected=NullPointerException.class)
  public void cannotRegisterNullCallback() {
    WroManager manager = new WroManager();
    manager.registerCallback(null);
  }
  
  /**
   * Ignored because it fails when running the test from command line.
   */
  //@Ignore
  @Test
  public void testFromFolder()
    throws Exception {
    final ResourcePreProcessor processor = new WroManagerProcessor();
    final URL url = getClass().getResource("wroManager");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
    //WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
  }


  /**
   * Initialize {@link WroConfiguration} object with cacheUpdatePeriod & modelUpdatePeriod equal with provided argument.
   */
  private WroConfiguration newConfigWithUpdatePeriodValue(final long periodValue) {
    final WroConfiguration config = new WroConfiguration();
    config.setCacheUpdatePeriod(periodValue);
    config.setModelUpdatePeriod(periodValue);
    config.setDisableCache(true);
    return config;
  }


  @Test
  public void testNoProcessorWroManagerFactory()
    throws IOException {
    final WroManagerFactory factory = new NoProcessorsWroManagerFactory();

    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Context.get().getResponse();

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");

    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));

    final WroManager manager = factory.create();
    manager.setModelFactory(getValidModelFactory());

    manager.process();

    // compare written bytes to output stream with the content from specified css.
    WroTestUtils.compare(WroTestUtils.getInputStream("classpath:ro/isdc/wro/manager/noProcessorsResult.css"),
      new ByteArrayInputStream(out.toByteArray()));
  }

  @Test
  public void testDuplicatedResourcesShouldBeSkipped()
    throws Exception {
    new GenericTestBuilder().processAndCompare("/repeatedResources.js", "classpath:ro/isdc/wro/manager/repeated-out.js");
  }


  @Test
  public void testWildcardDuplicatedResourcesShouldBeSkiped()
    throws Exception {
    new GenericTestBuilder().processAndCompare("/wildcardRepeatedResources.js",
      "classpath:ro/isdc/wro/manager/wildcardRepeated-out.js");
  }


  @Test
  public void testMinimizeAttributeIsFalseOnResource()
    throws Exception {
    new GenericTestBuilder().processAndCompare("/resourceMinimizeFalse.js", "classpath:ro/isdc/wro/manager/sample.js");
  }


  @Test
  public void testMinimizeAttributeIsTrueOnResource()
    throws Exception {
    new GenericTestBuilder().processAndCompare("/resourceMinimizeTrue.js",
      "classpath:ro/isdc/wro/manager/sample.min.js");
  }


  @Test
  public void testWildcardGroupResources()
    throws Exception {
    new GenericTestBuilder().processAndCompare("/wildcardResources.js", "classpath:ro/isdc/wro/manager/wildcard-out.js");
  }


  /**
   * Test that when ignoreMissingResource is true and IOException is thrown by a processor, no exception is thrown.
   *
   * @throws Exception
   */
  @Test
  public void testCssWithInvalidImport()
    throws Exception {
    new GenericTestBuilder().processAndCompare("/invalidImport.css",
      "classpath:ro/isdc/wro/manager/invalidImport-out.css");
  }


  @Test(expected = WroRuntimeException.class)
  public void testCssWithInvalidImportAndIgnoreFalse()
    throws Exception {
    new GenericTestBuilder() {
      @Override
      protected void onBeforeProcess() {
        Context.get().getConfig().setIgnoreMissingResources(false);
      };
    }.processAndCompare("/invalidImport.css", "classpath:ro/isdc/wro/manager/invalidImport-out.css");
  }


  @Test
  public void processValidModel()
    throws IOException {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");

    // Test also that ETag header value contains quotes
    Mockito.doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
        throws Throwable {
        LOG.debug("Header: {}", Arrays.toString(invocation.getArguments()));
        final Object[] arguments = invocation.getArguments();
        if (HttpHeader.ETAG.toString().equals(arguments[0])) {
          final String etagHeaderValue = (String)arguments[1];
          Assert.assertTrue(etagHeaderValue.matches("\".*?\""));
        }
        return null;
      }
    }).when(response).setHeader(Mockito.anyString(), Mockito.anyString());

    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));

    managerFactory.create().process();
  }


  @Test
  public void testManagerWithSchedulerAndUpdatePeriodSet()
    throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");
    final Context context = Context.webContext(request,
      Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS), Mockito.mock(FilterConfig.class));
    final WroConfiguration config = new WroConfiguration();
    // make it run each 1 second
    config.setModelUpdatePeriod(1);
    config.setCacheUpdatePeriod(1);
    Context.set(context, config);

    managerFactory.create().process();
    // let scheduler run a while
    Thread.sleep(1300);
  }


  /**
   * Test how manager behaves when the update period value is greater than zero and the scheduler starts.
   *
   * @throws Exception
   */
  @Test
  public void testManagerWhenSchedulerIsStarted()
    throws Exception {
    newConfigWithUpdatePeriodValue(1);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Context.get().getResponse();
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");

    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));


    managerFactory.create().process();
    // allow thread to do its job
    Thread.sleep(500);
  }

  @Test
  public void testAggregatedComputedFolder() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Context.get().getResponse();
    Mockito.when(request.getRequestURI()).thenReturn("/wro4j/wro/g1.css");

    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));

    managerFactory.create().process();

    Assert.assertEquals("/wro4j/wro/", Context.get().getAggregatedFolderPath());
  }

  @Test
  public void testAggregatedComputedFolder2() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Context.get().getResponse();
    Mockito.when(request.getRequestURI()).thenReturn("/wro4j/wro/path/to/g1.css");

    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));

    managerFactory.create().process();

    Assert.assertEquals("/wro4j/wro/path/to/", Context.get().getAggregatedFolderPath());
  }

  @Test(expected = UnauthorizedRequestException.class)
  public void testProxyUnauthorizedRequest()
    throws Exception {
    processProxyWithResourceId("test");
  }


  private void processProxyWithResourceId(final String resourceId)
    throws IOException {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID)).thenReturn(resourceId);
    Mockito.when(request.getRequestURI()).thenReturn(
      CssUrlRewritingProcessor.PATH_RESOURCES + "?" + CssUrlRewritingProcessor.PARAM_RESOURCE_ID + "=" + resourceId);

    final WroConfiguration config = new WroConfiguration();
    // we don't need caching here, otherwise we'll have clashing during unit tests.
    config.setDisableCache(true);

    Context.set(
      Context.webContext(request, Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS),
        Mockito.mock(FilterConfig.class)), newConfigWithUpdatePeriodValue(0));
    managerFactory.create().process();
  }


  @Test
  public void testCRC32Fingerprint()
    throws Exception {
    final WroManager manager = managerFactory.create();
    manager.setHashBuilder(new CRC32HashBuilder());
    final String path = manager.encodeVersionIntoGroupPath("g3", ResourceType.CSS, true);
    Assert.assertEquals("daa1bb3c/g3.css?minimize=true", path);
  }


  @Test
  public void testMD5Fingerprint()
    throws Exception {
    final WroManager manager = managerFactory.create();
    manager.setHashBuilder(new MD5HashBuilder());
    final String path = manager.encodeVersionIntoGroupPath("g3", ResourceType.CSS, true);
    Assert.assertEquals("42b98f2980dc1366cf1d2677d4891eda/g3.css?minimize=true", path);
  }


  @Test
  public void testSHA1DefaultHashBuilder()
    throws Exception {
    final WroManager manager = managerFactory.create();
    final String path = manager.encodeVersionIntoGroupPath("g3", ResourceType.CSS, true);
    Assert.assertEquals("51e6de8dde498cb0bf082b2cd80323fca19eef5/g3.css?minimize=true", path);
  }

  @Test
  public void cacheShouldBeClearedAfterModelReload() throws IOException {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(request.getRequestURI()).thenReturn("/app/g3.css");

    final WroConfiguration config = new WroConfiguration();	    
	config.setDebug(true);
	config.setDisableCache(false);
    
    Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));

    final WroManager wroManager = managerFactory.create();
    wroManager.process();
    
    Assert.assertNotNull(wroManager.getCacheStrategy().get(new CacheEntry("g3", ResourceType.CSS, true)));
    
    final ReloadModelRunnable reloadModelRunnable = new ReloadModelRunnable(wroManager);
    reloadModelRunnable.run();
    Assert.assertNull(wroManager.getCacheStrategy().get(new CacheEntry("g3", ResourceType.CSS, true)));
  }
  
  
  @After
  public void tearDown() {
    managerFactory.destroy();
    Context.unset();
  }
}
