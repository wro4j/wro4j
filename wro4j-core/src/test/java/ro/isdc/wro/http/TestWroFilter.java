/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import static org.mockito.Mockito.when;
import static ro.isdc.wro.http.handler.ResourceProxyRequestHandler.PARAM_RESOURCE_ID;
import static ro.isdc.wro.http.handler.ResourceProxyRequestHandler.PATH_RESOURCES;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.DefaultContext;
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
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.ObjectFactory;
import ro.isdc.wro.util.WroUtil;


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
  private UriLocator mockUriLocator;
  
  @Before
  public void setUp()
      throws Exception {
    DefaultContext.set(DefaultContext.standaloneContext());
    MockitoAnnotations.initMocks(this);
    
    when(mockUriLocatorFactory.getInstance(Mockito.anyString())).thenReturn(mockUriLocator);
    when(mockUriLocator.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);
    when(mockUriLocatorFactory.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);

    when(mockRequest.getAttribute(Mockito.anyString())).thenReturn(null);
    when(mockManagerFactory.create()).thenReturn(new BaseWroManagerFactory().create());
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
    
    victim = new WroFilter() {
      @Override
      protected void onRuntimeException(final RuntimeException e, final HttpServletResponse response,
          final FilterChain chain) {
        throw e;
      }
    };
    victim.setWroManagerFactory(mockManagerFactory);
    initFilter(victim);
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
    initFilter(victim);
  }

  private void initFilter(final WroFilter filter)
      throws ServletException {
    filter.init(mockFilterConfig);
  }
  
  /**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test(expected = WroRuntimeException.class)
  public void testFilterInitParamsAreWrong()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.name())).thenReturn(
        "InvalidNumber");
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.modelUpdatePeriod.name())).thenReturn("100");
    victim.init(mockFilterConfig);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotAcceptInvalidAppFactoryClassNameIsSet()
      throws Exception {
    victim = new WroFilter();
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn(
        "Invalid value");
    victim.init(mockFilterConfig);
  }
  
  @Test
  public void shouldUseInitiallySetManagerEvenIfAnInvalidAppFactoryClassNameIsSet()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn(
        "Invalid value");
    victim.init(mockFilterConfig);
    Assert.assertSame(mockManagerFactory, victim.getWroManagerFactory());
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
    Mockito.when(mockManagerFactory.create()).thenThrow(e);
    
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }
  
  @Test
  public void testValidAppFactoryClassNameIsSet()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn(
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
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.managerFactoryClassName.name())).thenReturn(
        managerClass.getName());
    
    victim.init(mockFilterConfig);
    Class<?> actualClass = ((DefaultWroManagerFactory) victim.getWroManagerFactory()).getFactory().getClass();
    Assert.assertSame(managerClass, actualClass);
  }
  
  public static class TestWroManagerFactory
      extends BaseWroManagerFactory {
  }
  
  @Test
  public void testJmxDisabled()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.jmxEnabled.name())).thenReturn("false");
    victim.init(mockFilterConfig);
  }
  
  /**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test
  public void testFilterInitParamsAreSetProperly()
      throws Exception {
    setConfigurationMode(FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.gzipResources.name())).thenReturn("false");
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.cacheUpdatePeriod.name())).thenReturn("10");
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.modelUpdatePeriod.name())).thenReturn("100");
    victim.init(mockFilterConfig);
    final WroConfiguration config = victim.getWroConfiguration();
    Assert.assertEquals(false, config.isDebug());
    Assert.assertEquals(false, config.isGzipEnabled());
    Assert.assertEquals(10, config.getCacheUpdatePeriod());
    Assert.assertEquals(100, config.getModelUpdatePeriod());
  }
  
  @Test
  public void testValidHeaderParamIsSet()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.header.name())).thenReturn("ETag: 998989");
    victim.init(mockFilterConfig);
  }
  
  @Test
  public void testValidHeaderParamsAreSet()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.header.name())).thenReturn(
        "ETag: 998989 | Expires: Thu, 15 Apr 2010 20:00:00 GMT");
    victim.init(mockFilterConfig);
  }
  
  @Test(expected = WroRuntimeException.class)
  public void testInvalidHeaderParamIsSet()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.header.name())).thenReturn("ETag 998989 expires 1");
    victim.init(mockFilterConfig);
  }
  
  /**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test
  public void testConfigurationInitParam()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
        "anyOtherString");
    victim.init(mockFilterConfig);
    Assert.assertEquals(true, victim.getWroConfiguration().isDebug());
  }
  
  @Test
  public void testDisableCacheInitParamInDeploymentMode()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
        FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.disableCache.name())).thenReturn("true");
    victim.init(mockFilterConfig);
    Assert.assertEquals(false, victim.getWroConfiguration().isDebug());
    Assert.assertEquals(false, victim.getWroConfiguration().isDisableCache());
  }
  
  @Test
  public void testDisableCacheInitParamInDevelopmentMode()
      throws Exception {
    Mockito.when(mockFilterConfig.getInitParameter(ConfigConstants.disableCache.name())).thenReturn("true");
    victim.init(mockFilterConfig);
    Assert.assertEquals(true, victim.getWroConfiguration().isDebug());
    Assert.assertEquals(true, victim.getWroConfiguration().isDisableCache());
  }
  
  /**
   * Check what happens when the request cannot be processed and assure that the we proceed with chain.
   * 
   * @throws Exception
   */
  public void cannotProcessConfigResourceStream()
      throws Exception {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("");
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verifyChainIsCalled(mockFilterChain);
  }
  
  /**
   * Check if the chain call was performed.
   */
  private void verifyChainIsCalled(final FilterChain chain)
      throws IOException, ServletException {
    Mockito.verify(chain, Mockito.atLeastOnce()).doFilter(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
  }
  
  /**
   * Check if the chain call was performed.
   */
  private void verifyChainIsNotCalled(final FilterChain chain)
      throws IOException, ServletException {
    Mockito.verify(chain, Mockito.never()).doFilter(Mockito.any(HttpServletRequest.class),
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
    
    Mockito.when(mockAuthorizationManager.isAuthorized(resourcePath)).thenReturn(true);
    requestGroupByUri(requestUri, new RequestBuilder(requestUri) {
      @Override
      protected HttpServletRequest newRequest() {
        final HttpServletRequest request = super.newRequest();
        Mockito.when(request.getParameter(PARAM_RESOURCE_ID)).thenReturn(resourcePath);
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
        return new InjectorBuilder().setWroManager(
            new BaseWroManagerFactory().setUriLocatorFactory(mockUriLocatorFactory).create()).setResourceAuthorizationManager(
            mockAuthorizationManager).build();
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
        Mockito.when(request.getParameter(PARAM_RESOURCE_ID)).thenReturn(resourcePath);
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
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/g2.js");
    victim.setWroManagerFactory(createValidManagerFactory());
    setConfigurationMode(FilterConfigWroConfigurationFactory.PARAM_VALUE_DEPLOYMENT);
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
    final ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
    Mockito.when(mockResponse.getOutputStream()).thenReturn(sos);
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
    Mockito.when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.PATH_API + "/someMethod");
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
    Mockito.when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.PATH_API + "/someMethod");
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
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(request.getRequestURI()).thenReturn(ReloadCacheRequestHandler.ENDPOINT_URI);
    
    Mockito.when(mockResponse.getWriter()).thenReturn(new PrintWriter(System.out));
    final FilterChain chain = Mockito.mock(FilterChain.class);
    
    final CacheStrategy<CacheEntry, ContentHashEntry> mockCacheStrategy = Mockito.mock(CacheStrategy.class);
    
    WroManagerFactory managerFactory = new BaseWroManagerFactory().setCacheStrategy(mockCacheStrategy);
    
    victim.setWroManagerFactory(managerFactory);
    // by default configuration is development
    victim.init(mockFilterConfig);
    
    victim.doFilter(request, mockResponse, chain);
    // api method exposed -> chain is not called
    verifyChainIsNotCalled(chain);
    
    Mockito.verify(mockCacheStrategy).clear();
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
      protected ObjectFactory<WroConfiguration> newWroConfigurationFactory() {
        final PropertyWroConfigurationFactory factory = new PropertyWroConfigurationFactory(props);
        return factory;
      }
    };
    // initFilterWithValidConfig(theFilter);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(request.getRequestURI()).thenReturn(ReloadCacheRequestHandler.ENDPOINT_URI);
    
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(response.getWriter()).thenReturn(new PrintWriter(System.out));
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
    DefaultContext.set(DefaultContext.standaloneContext());
    
    final WroFilter filter = new WroFilter() {
      @Override
      protected WroManagerFactory newWroManagerFactory() {
        return wroManagerFactory;
      }
      
      @Override
      protected ObjectFactory<WroConfiguration> newWroConfigurationFactory() {
        return new ObjectFactory<WroConfiguration>() {
          public WroConfiguration create() {
            return DefaultContext.get().getConfig();
          }
        };
      }
    };
    filter.init(mockFilterConfig);
    final WroModelFactory modelFactory = wroManagerFactory.create().getModelFactory();
    
    Assert.assertTrue(modelFactory.create().getGroups().isEmpty());
    
    // reload model
    DefaultContext.get().getConfig().reloadModel();
    // the second time should have one group
    Assert.assertEquals(1, modelFactory.create().getGroups().size());
  }
  
  @Test
  public void testReloadCacheCall()
      throws Exception {
    Mockito.when(mockRequest.getRequestURI()).thenReturn(ReloadCacheRequestHandler.ENDPOINT_URI);
    
    final ThreadLocal<Integer> status = new ThreadLocal<Integer>();
    final HttpServletResponse response = new HttpServletResponseWrapper(mockResponse) {
      @Override
      public void setStatus(final int sc) {
        status.set(sc);
      }
    };
    
    DefaultContext.set(DefaultContext.webContext(mockRequest, response, mockFilterConfig));
    victim.doFilter(DefaultContext.get().getRequest(), DefaultContext.get().getResponse(), mockFilterChain);
    
    Assert.assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), status.get());
  }
  
  @Test
  public void testReloadModelCall()
      throws Exception {
    Mockito.when(mockRequest.getRequestURI()).thenReturn(ReloadModelRequestHandler.ENDPOINT_URI);
    
    final ThreadLocal<Integer> status = new ThreadLocal<Integer>();
    final HttpServletResponse response = new HttpServletResponseWrapper(Mockito.mock(HttpServletResponse.class,
        Mockito.RETURNS_DEEP_STUBS)) {
      @Override
      public void setStatus(final int sc) {
        status.set(sc);
      }
    };
    
    DefaultContext.set(DefaultContext.webContext(mockRequest, response, Mockito.mock(FilterConfig.class)));
    victim.doFilter(DefaultContext.get().getRequest(), DefaultContext.get().getResponse(), mockFilterChain);
    Assert.assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), status.get());
  }
  
  /**
   * Mocks the WroFilter.PARAM_CONFIGURATION init param with passed value.
   */
  private void setConfigurationMode(final String value) {
    Mockito.when(mockFilterConfig.getInitParameter(FilterConfigWroConfigurationFactory.PARAM_CONFIGURATION)).thenReturn(
        value);
  }
  
  class RequestBuilder {
    private final String requestUri;
    
    public RequestBuilder(final String requestUri) {
      this.requestUri = requestUri;
    }
    
    protected HttpServletRequest newRequest() {
      Mockito.when(mockRequest.getRequestURI()).thenReturn(requestUri);
      return mockRequest;
    }
  }
  
  /**
   * Should throw {@link NullPointerException} when provided requestHandler's collection is null.
   */
  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullRequestHandlers()
      throws Exception {
    victim.setRequestHandlerFactory(new RequestHandlerFactory() {
      public Collection<RequestHandler> create() {
        return null;
      }
    });
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
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
    Mockito.when(mockRequest.getParameter(PARAM_RESOURCE_ID)).thenReturn(resourceId);
    Mockito.when(mockRequest.getRequestURI()).thenReturn(PATH_RESOURCES + "?" + PARAM_RESOURCE_ID + "=" + resourceId);
    
    final WroConfiguration config = new WroConfiguration();
    // we don't need caching here, otherwise we'll have clashing during unit tests.
    config.setDisableCache(true);
    
    DefaultContext.set(DefaultContext.webContext(mockRequest,
        Mockito.mock(HttpServletResponse.class, Mockito.RETURNS_DEEP_STUBS),
            Mockito.mock(FilterConfig.class)), newConfigWithUpdatePeriodValue(0));
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

  @After
  public void tearDown() {
    if (victim != null) {
      victim.destroy();
    }
    DefaultContext.unset();
  }
}
