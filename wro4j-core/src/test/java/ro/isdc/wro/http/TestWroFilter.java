/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ro.isdc.wro.http.handler.ResourceProxyRequestHandler.PARAM_RESOURCE_ID;
import static ro.isdc.wro.http.handler.ResourceProxyRequestHandler.PATH_RESOURCES;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.factory.FilterConfigWroConfigurationFactory;
import ro.isdc.wro.config.factory.PropertyWroConfigurationFactory;
import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.handler.ReloadCacheRequestHandler;
import ro.isdc.wro.http.handler.ReloadModelRequestHandler;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.factory.RequestHandlerFactory;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.DefaultWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroUtil;
import ro.isdc.wro.util.io.NullOutputStream;


/**
 * Test for {@link WroFilter} class.
 *
 * @author Alex Objelean
 * @created Created on Jul 13, 2009
 */
public class TestWroFilter {
  private WroFilter victim;
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterChain mockFilterChain;
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private WroManagerFactory mockManagerFactory;
  @Mock
  private ResourceAuthorizationManager mockAuthorizationManager;
  @Mock
  private UriLocatorFactory mockUriLocatorFactory;
  @Mock
  private MBeanServer mockMBeanServer;
  @Mock
  private UriLocator mockUriLocator;

  @Before
  public void setUp()
      throws Exception {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);

    when(mockUriLocatorFactory.getInstance(Mockito.anyString())).thenReturn(mockUriLocator);
    when(mockUriLocator.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);
    when(mockUriLocatorFactory.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);

    when(mockRequest.getAttribute(Mockito.anyString())).thenReturn(null);
    when(mockManagerFactory.create()).thenReturn(new BaseWroManagerFactory().create());
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new NullOutputStream()));

    victim = new WroFilter() {
      @Override
      protected void onException(final Exception e, final HttpServletResponse response, final FilterChain chain) {
        throw WroRuntimeException.wrap(e);
      }

      @Override
      protected MBeanServer getMBeanServer() {
        return mockMBeanServer;
      }
    };
    victim.setWroManagerFactory(mockManagerFactory);
  }

  private WroManagerFactory createValidManagerFactory() {
    return new BaseWroManagerFactory().setModelFactory(createValidModelFactory());
  }

  private WroModelFactory createValidModelFactory() {
    return new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream() {
        return TestWroFilter.class.getResourceAsStream("wro.xml");
      }
    };
  }

  private void initChainOnErrorFilter()
      throws ServletException {
    victim = new WroFilter();
    victim.init(mockFilterConfig);
  }

  /**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test(expected = WroRuntimeException.class)
  public void testFilterInitParamsAreWrong()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.name())).thenReturn("InvalidNumber");
    when(mockFilterConfig.getInitParameter(ConfigConstants.modelUpdatePeriod.name())).thenReturn("100");
    victim.init(mockFilterConfig);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidAppFactoryClassNameIsSet()
      throws Exception {
    victim = new WroFilter();
    when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn("Invalid value");
    victim.init(mockFilterConfig);
  }

  @Test
  public void shouldUseInitiallySetManagerEvenIfAnInvalidAppFactoryClassNameIsSet()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn("Invalid value");
    victim.init(mockFilterConfig);

    Assert.assertSame(mockManagerFactory, AbstractDecorator.getOriginalDecoratedObject(victim.getWroManagerFactory()));
  }

  /**
   * Test that in DEPLOYMENT mode if {@link InvalidGroupNameException} is thrown, the response redirect to 404.
   */
  @Test
  public void testInvalidGroupNameExceptionThrownInDEPLOYMENTMode()
      throws Exception {
    testChainContinueWhenSpecificExceptionThrown(new InvalidGroupNameException(""));
  }

  /**
   * Test that in DEPLOYMENT mode if {@link InvalidGroupNameException} is thrown, the response redirect to 404.
   */
  @Test
  public void testUnauthorizedRequestExceptionThrownInDEPLOYMENTMode()
      throws Exception {
    testChainContinueWhenSpecificExceptionThrown(new UnauthorizedRequestException(""));
  }

  /**
   * Test that in DEPLOYMENT mode if specified exception is thrown, the response redirect to 404.
   */
  public void testChainContinueWhenSpecificExceptionThrown(final Throwable e)
      throws Exception {
    initChainOnErrorFilter();
    when(mockManagerFactory.create()).thenThrow(e);

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }

  @Test
  public void testValidAppFactoryClassNameIsSet()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn(
        BaseWroManagerFactory.class.getName());
    victim.init(mockFilterConfig);
  }

  /**
   * Test that when setting WwroManagerFactory via setter, even if wroConfiguration has a different
   * {@link WroManagerFactory} configured, the first one instance is used.
   */
  @Test
  public void shouldUseCorrectWroManagerFactoryWhenOneIsSet()
      throws Exception {
    final Class<?> managerClass = TestWroManagerFactory.class;
    victim.setWroManagerFactory(null);
    when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn(
        managerClass.getName());

    victim.init(mockFilterConfig);
    final Class<?> actualClass = ((DefaultWroManagerFactory) AbstractDecorator.getOriginalDecoratedObject(victim.getWroManagerFactory())).getFactory().getClass();
    Assert.assertSame(managerClass, actualClass);
  }

  public static class TestWroManagerFactory
      extends BaseWroManagerFactory {
  }

  @Test
  public void testJmxDisabled()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.jmxEnabled.name())).thenReturn("false");
    victim.init(mockFilterConfig);
  }

  /**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test
  public void testFilterInitParamsAreSetProperly()
      throws Exception {
    setConfigurationMode(FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
    when(mockFilterConfig.getInitParameter(ConfigConstants.gzipResources.name())).thenReturn("false");
    when(mockFilterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.name())).thenReturn("10");
    when(mockFilterConfig.getInitParameter(ConfigConstants.modelUpdatePeriod.name())).thenReturn("100");
    victim.init(mockFilterConfig);
    final WroConfiguration config = victim.getConfiguration();
    Assert.assertEquals(false, config.isDebug());
    Assert.assertEquals(false, config.isGzipEnabled());
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(100, config.getModelUpdatePeriod());
  }

  @Test
  public void testValidHeaderParamIsSet()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.header.name())).thenReturn("ETag: 998989");
    victim.init(mockFilterConfig);
  }

  @Test
  public void testValidHeaderParamsAreSet()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.header.name())).thenReturn(
        "ETag: 998989 | Expires: Thu, 15 Apr 2010 20:00:00 GMT");
    victim.init(mockFilterConfig);
  }

  @Test(expected = WroRuntimeException.class)
  public void testInvalidHeaderParamIsSet()
      throws Exception {
    // this test fails only when debug is turned off.
    when(mockFilterConfig.getInitParameter(ConfigConstants.header.name())).thenReturn("ETag 998989 expires 1");
    victim.init(mockFilterConfig);
  }

  /**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test
  public void testConfigurationInitParam()
      throws Exception {
    when(mockFilterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
        "anyOtherString");
    victim.init(mockFilterConfig);
    Assert.assertEquals(true, victim.getConfiguration().isDebug());
  }

  @Test
  public void testDisableCacheInitParamInDeploymentMode()
      throws Exception {
    when(mockFilterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
        FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
    when(mockFilterConfig.getInitParameter(ConfigConstants.disableCache.name())).thenReturn("true");
    victim.init(mockFilterConfig);
    Assert.assertEquals(false, victim.getConfiguration().isDebug());
    Assert.assertEquals(false, victim.getConfiguration().isDisableCache());
  }

  @Test
  public void testDisableCacheInitParamInDevelopmentMode()
      throws Exception {
    when(mockFilterConfig.getInitParameter(ConfigConstants.disableCache.name())).thenReturn("true");
    victim.init(mockFilterConfig);
    Assert.assertEquals(true, victim.getConfiguration().isDebug());
    Assert.assertEquals(true, victim.getConfiguration().isDisableCache());
  }

  /**
   * Check what happens when the request cannot be processed and assure that the we proceed with chain.
   *
   * @throws Exception
   */
  public void cannotProcessConfigResourceStream()
      throws Exception {
    when(mockRequest.getRequestURI()).thenReturn("");
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }

  /**
   * Check if the chain call was performed.
   */
  private void verifyChainIsCalled(final FilterChain chain)
      throws IOException, ServletException {
    verify(chain, Mockito.atLeastOnce()).doFilter(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
  }

  /**
   * Check if the chain call was performed.
   */
  private void verifyChainIsNotCalled(final FilterChain chain)
      throws IOException, ServletException {
    verify(chain, Mockito.never()).doFilter(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
  }

  @Test
  public void cannotProcessInvalidUri()
      throws Exception {
    initChainOnErrorFilter();
    requestGroupByUri("", mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }

  @Test
  public void requestValidGroup()
      throws Exception {
    initChainOnErrorFilter();
    requestGroupByUri("/folder/g1.css");
  }

  @Test
  public void requestInvalidGroup()
      throws Exception {
    initChainOnErrorFilter();
    requestGroupByUri("/folder/INVALID_GROUP.css", mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }

  @Test
  public void cannotAccessUnauthorizedRequest()
      throws Exception {
    initVictimWithMockAuthManager();
    final String resourcePath = "/g1.css";
    final String requestUri = PATH_RESOURCES + resourcePath;

    when(mockAuthorizationManager.isAuthorized(resourcePath)).thenReturn(true);
    requestGroupByUri(requestUri, new RequestBuilder(requestUri) {
      @Override
      protected HttpServletRequest newRequest() {
        final HttpServletRequest request = super.newRequest();
        when(request.getParameter(PARAM_RESOURCE_ID)).thenReturn(resourcePath);
        return request;
      }
    }, mockFilterChain);
    verifyChainIsNotCalled(mockFilterChain);
  }

  /**
   * Creates the victim filter which usues mock {@link ResourceAuthorizationManager}.
   */
  private void initVictimWithMockAuthManager() {
    victim = new WroFilter() {
      @Override
      protected void onRuntimeException(final RuntimeException e, final HttpServletResponse response,
          final FilterChain chain) {
        throw e;
      }

      @Override
      Injector getInjector() {
        return new InjectorBuilder(
            new BaseWroManagerFactory().setUriLocatorFactory(mockUriLocatorFactory).setResourceAuthorizationManager(
                mockAuthorizationManager)).build();
      }
    };
  }

  @Test
  public void requestUrlRewrittenResource()
      throws Exception {
    initVictimWithMockAuthManager();
    final String resourcePath = "classpath:ro/isdc/wro/http/2.css";

    when(mockAuthorizationManager.isAuthorized(resourcePath)).thenReturn(true);

    final String requestUri = PATH_RESOURCES + "?id=" + resourcePath;
    requestGroupByUri(requestUri, new RequestBuilder(requestUri) {
      @Override
      protected HttpServletRequest newRequest() {
        final HttpServletRequest request = super.newRequest();
        when(request.getParameter(PARAM_RESOURCE_ID)).thenReturn(resourcePath);
        return request;
      }
    });
  }

  private void requestGroupByUri(final String requestUri)
      throws Exception {
    requestGroupByUri(requestUri, new RequestBuilder(requestUri), mockFilterChain);
  }

  private void requestGroupByUri(final String requestUri, final FilterChain chain)
      throws Exception {
    requestGroupByUri(requestUri, new RequestBuilder(requestUri), chain);
  }

  @Test
  public void testDoFilterInDEPLOYMENTMode()
      throws Exception {
    when(mockRequest.getRequestURI()).thenReturn("/g2.js");
    victim.setWroManagerFactory(createValidManagerFactory());
    setConfigurationMode(FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
    victim.init(mockFilterConfig);

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
  }

  /**
   * Perform initialization and simulates a call to WroFilter with given requestUri.
   *
   * @param requestUri
   */
  private void requestGroupByUri(final String requestUri, final RequestBuilder requestBuilder, final FilterChain chain)
      throws Exception {
    final HttpServletRequest request = requestBuilder.newRequest();
    final ServletOutputStream sos = mock(ServletOutputStream.class);
    when(mockResponse.getOutputStream()).thenReturn(sos);
    victim.init(mockFilterConfig);
    victim.doFilter(request, mockResponse, chain);
  }

  private void requestGroupByUri(final String requestUri, final RequestBuilder requestBuilder)
      throws Exception {
    requestGroupByUri(requestUri, requestBuilder, mockFilterChain);
  }

  /**
   * Tests that in DEPLOYMENT mode the API is not exposed.
   */
  @Test
  public void testApiCallInDEPLOYMENTMode()
      throws Exception {
    initChainOnErrorFilter();
    when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.PATH_API + "/someMethod");
    setConfigurationMode(FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    // No api method exposed -> proceed with chain
    verifyChainIsCalled(mockFilterChain);
  }

  /**
   * Tests that in DEPLOYMENT mode the API is not exposed.
   */
  @Test
  public void testApiCallInDEVELOPMENTModeAndInvalidApiCall()
      throws Exception {
    initChainOnErrorFilter();
    when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.PATH_API + "/someMethod");
    // by default configuration is development
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    // No api method exposed -> proceed with chain
    verifyChainIsCalled(mockFilterChain);
  }

  /**
   * Tests that in DEVELOPMENT mode the API is exposed.
   */
  @Test
  public void testApiCallInDEVELOPMENTModeAndReloadCacheCall()
      throws Exception {
    when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.ENDPOINT_URI);
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(System.out));

    final CacheStrategy<CacheKey, CacheValue> mockCacheStrategy = mock(CacheStrategy.class);

    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setCacheStrategy(mockCacheStrategy);

    victim.setWroManagerFactory(managerFactory);
    // by default configuration is development
    victim.init(mockFilterConfig);

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    // api method exposed -> chain is not called
    verifyChainIsNotCalled(mockFilterChain);

    verify(mockCacheStrategy).clear();
  }

  /**
   * Tests that in DEPLOYMENT mode the API is NOT exposed.
   */
  @Test
  public void apiCallInDeploymentMode()
      throws Exception {
    final Properties props = new Properties();
    // init WroConfig properties
    props.setProperty(ConfigConstants.debug.name(), Boolean.FALSE.toString());
    final WroFilter theFilter = new WroFilter() {
      @Override
      protected ObjectFactory<WroConfiguration> newWroConfigurationFactory(final FilterConfig filterConfig) {
        final PropertyWroConfigurationFactory factory = new PropertyWroConfigurationFactory(props);
        return factory;
      }
    };
    // initFilterWithValidConfig(theFilter);
    final HttpServletRequest request = mock(HttpServletRequest.class, Mockito.RETURNS_DEEP_STUBS);
    when(request.getRequestURI()).thenReturn(ReloadCacheRequestHandler.ENDPOINT_URI);

    final HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(System.out));
    // by default configuration is development
    theFilter.init(mockFilterConfig);

    theFilter.doFilter(request, response, mockFilterChain);
    // No api method exposed -> proceed with chain
    verifyChainIsCalled(mockFilterChain);
  }

  /**
   * Proves that the model reload has effect.
   */
  @Test
  public void modelShouldBeReloadedWhenReloadIsTriggered()
      throws Exception {
    final WroManagerFactory wroManagerFactory = new BaseWroManagerFactory().setModelFactory(new WroModelFactory() {
      private boolean wasCreated = false;

      public WroModel create() {
        if (!wasCreated) {
          wasCreated = true;
          // return model with no groups defined
          return new WroModel();
        }
        // second time when created add one group
        return new WroModel().addGroup(new Group("g1"));
      }

      public void destroy() {
      }
    });
    Context.set(Context.standaloneContext());

    final WroFilter filter = new WroFilter() {
      @Override
      protected WroManagerFactory newWroManagerFactory() {
        return wroManagerFactory;
      }

      @Override
      protected ObjectFactory<WroConfiguration> newWroConfigurationFactory(final FilterConfig filterConfig) {
        return new ObjectFactory<WroConfiguration>() {
          public WroConfiguration create() {
            return Context.get().getConfig();
          }
        };
      }
    };
    filter.init(mockFilterConfig);
    final WroModelFactory modelFactory = wroManagerFactory.create().getModelFactory();

    assertTrue(modelFactory.create().getGroups().isEmpty());

    // reload model
    Context.get().getConfig().reloadModel();
    // the second time should have one group
    assertEquals(1, modelFactory.create().getGroups().size());
  }

  @Test
  public void testReloadCacheCall()
      throws Exception {
    when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.ENDPOINT_URI);

    final ThreadLocal<Integer> status = new ThreadLocal<Integer>();
    final HttpServletResponse response = new HttpServletResponseWrapper(mockResponse) {
      @Override
      public void setStatus(final int sc) {
        status.set(sc);
      }
    };

    Context.set(Context.webContext(mockRequest, response, mockFilterConfig));
    victim.init(mockFilterConfig);
    victim.doFilter(Context.get().getRequest(), Context.get().getResponse(), mockFilterChain);

    assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), status.get());
  }

  @Test
  public void testReloadModelCall()
      throws Exception {
    when(mockRequest.getRequestURI()).thenReturn(ReloadModelRequestHandler.ENDPOINT_URI);

    final ThreadLocal<Integer> status = new ThreadLocal<Integer>();
    final HttpServletResponse response = new HttpServletResponseWrapper(mockResponse) {
      @Override
      public void setStatus(final int sc) {
        status.set(sc);
      }
    };

    Context.set(Context.webContext(mockRequest, response, mockFilterConfig));
    victim.init(mockFilterConfig);
    victim.doFilter(Context.get().getRequest(), Context.get().getResponse(), mockFilterChain);
    assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), status.get());
  }

  /**
   * Mocks the WroFilter.PARAM_CONFIGURATION init param with passed value.
   */
  private void setConfigurationMode(final String value) {
    when(mockFilterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(value);
  }

  class RequestBuilder {
    private final String requestUri;

    public RequestBuilder(final String requestUri) {
      this.requestUri = requestUri;
    }

    protected HttpServletRequest newRequest() {
      when(mockRequest.getRequestURI()).thenReturn(requestUri);
      return mockRequest;
    }
  }

  /**
   * Should throw {@link NullPointerException} when provided requestHandler's collection is null.
   */
  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullRequestHandlers()
      throws Throwable {
    victim.setRequestHandlerFactory(new RequestHandlerFactory() {
      public Collection<RequestHandler> create() {
        return null;
      }
    });
    try {
      victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    } catch (final WroRuntimeException e) {
      throw e.getCause();
    }
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullRequestHandlerFactory() {
    victim.setRequestHandlerFactory(null);
  }

  @Test(expected = UnauthorizedRequestException.class)
  public void testProxyUnauthorizedRequest()
      throws Exception {
    processProxyWithResourceId("test");
  }

  private void processProxyWithResourceId(final String resourceId)
      throws Exception {
    when(mockRequest.getParameter(PARAM_RESOURCE_ID)).thenReturn(resourceId);
    when(mockRequest.getRequestURI()).thenReturn(PATH_RESOURCES + "?" + PARAM_RESOURCE_ID + "=" + resourceId);

    final WroConfiguration config = new WroConfiguration();
    // we don't need caching here, otherwise we'll have clashing during unit tests.
    config.setDisableCache(true);

    Context.set(Context.webContext(mockRequest, mockResponse, mockFilterConfig), newConfigWithUpdatePeriodValue(0));
    victim.init(mockFilterConfig);
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
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
  public void shouldDestroyWroModelWhenCacheIsDisabled()
      throws Exception {
    final WroConfiguration config = new WroConfiguration();
    config.setDisableCache(true);

    prepareValidRequest(config);

    final WroModelFactory mockModelFactory = Mockito.spy(createValidModelFactory());
    victim.setWroManagerFactory(new BaseWroManagerFactory().setModelFactory(mockModelFactory));

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockModelFactory).destroy();
  }

  private void prepareValidRequest(final WroConfiguration config)
      throws ServletException {
    when(mockRequest.getRequestURI()).thenReturn("/resource/g1.css");
    Context.set(Context.webContext(mockRequest, mockResponse, mockFilterConfig));
    victim.setConfiguration(config);
    victim.setWroManagerFactory(createValidManagerFactory());
    victim.init(mockFilterConfig);
  }

  @Test
  public void shouldNotDestroyWroModelWhenCacheIsNotDisabled()
      throws Exception {
    final WroConfiguration config = new WroConfiguration();
    config.setDisableCache(false);

    prepareValidRequest(config);

    final WroModelFactory mockModelFactory = Mockito.spy(createValidModelFactory());
    victim.setWroManagerFactory(new BaseWroManagerFactory().setModelFactory(mockModelFactory));

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockModelFactory, Mockito.never()).destroy();
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullConfiguration() {
    victim.setConfiguration(null);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailWhenConfigurationFactoryFails()
      throws Exception {
    victim = new WroFilter() {
      @Override
      protected ObjectFactory<WroConfiguration> newWroConfigurationFactory(final FilterConfig filterConfig) {
        throw new IllegalStateException("BOOM!");
      }
    };
    victim.init(mockFilterConfig);
  }

  @Test
  public void shouldChainTheIncludedRequestByDispatcher() throws Exception {
    when(mockRequest.getAttribute(DispatcherStreamLocator.ATTRIBUTE_INCLUDED_BY_DISPATCHER)).thenReturn(Boolean.TRUE);
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verify(mockManagerFactory, Mockito.never()).create();
    verifyChainIsCalled(mockFilterChain);
  }

  @Test
  public void shouldChainWhenFilterIsNotEnabled() throws Exception {
    victim.setEnable(false);
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }

  @Test
  public void shouldNotChainWhenFilterIsEnabled() throws Exception {
    prepareValidRequest(new WroConfiguration());
    victim.setEnable(true);

    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verifyChainIsNotCalled(mockFilterChain);
  }

  @Test
  public void shouldUnregisterMBeanOnDestroy() throws Exception {
    when(mockMBeanServer.isRegistered(Mockito.any(ObjectName.class))).thenReturn(true);
    victim.init(mockFilterConfig);
    victim.destroy();
    verify(mockMBeanServer).unregisterMBean(Mockito.any(ObjectName.class));
  }

  @After
  public void tearDown() {
    if (victim != null) {
      victim.destroy();
    }
    Context.unset();
  }
}
