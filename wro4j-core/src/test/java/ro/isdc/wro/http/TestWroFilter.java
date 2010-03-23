/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.InvalidGroupNameException;
import ro.isdc.wro.model.resource.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.test.util.WroTestUtils;

/**
 * Test for {@link WroFilter} class.
 *
 * @author Alex Objelean
 * @created Created on Jul 13, 2009
 */
public class TestWroFilter {
	private WroFilter filter;
	@Before
	public void initFilter() throws Exception {
		filter = new WroFilter();
		final FilterConfig config = Mockito.mock(FilterConfig.class);
		final ServletContext servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(config.getServletContext()).thenReturn(servletContext);
		filter.init(config);
	}

  /**
   * Initialize filter field with properly configured wro.xml.
   */
	private void initFilterWithValidConfig() {
		filter = new WroFilter() {
  		@Override
  		protected WroManagerFactory getWroManagerFactory() {
  			return new ServletContextAwareWroManagerFactory() {
  				@Override
  				protected WroModelFactory newModelFactory() {
  					return new XmlModelFactory() {
  	  	      @Override
  	  	      protected InputStream getConfigResourceAsStream() {
  	  	      	return WroTestUtils.getClassRelativeResource(TestWroFilter.class, "wro.xml");
  	  	      }
  					};
  				}
  	    };
  		}
  	};
	}

	/**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test(expected=WroRuntimeException.class)
  public void testFilterInitParamsAreWrong() throws Exception {
    final FilterConfig config = Mockito.mock(FilterConfig.class);
    Mockito.when(config.getInitParameter(WroFilter.PARAM_CACHE_UPDATE_PERIOD)).thenReturn("InvalidNumber");
    Mockito.when(config.getInitParameter(WroFilter.PARAM_MODEL_UPDATE_PERIOD)).thenReturn("100");
    filter.init(config);
  }

  @Test(expected=WroRuntimeException.class)
  public void testInvalidAppFactoryClassNameIsSet() throws Exception {
    final FilterConfig config = Mockito.mock(FilterConfig.class);
    Mockito.when(config.getInitParameter(filter.PARAM_MANAGER_FACTORY)).thenReturn("Invalid value");
    filter.init(config);
  }

  @Test
  public void testValidAppFactoryClassNameIsSet() throws Exception {
    final FilterConfig config = Mockito.mock(FilterConfig.class);
    Mockito.when(config.getInitParameter(filter.PARAM_MANAGER_FACTORY)).thenReturn(ServletContextAwareWroManagerFactory.class.getName());
    filter.init(config);
  }

	/**
	 * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
	 */
	@Test
	public void testFilterInitParamsAreSetProperly() throws Exception {
	  final FilterConfig config = Mockito.mock(FilterConfig.class);
	  Mockito.when(config.getInitParameter(WroFilter.PARAM_CONFIGURATION)).thenReturn(WroFilter.PARAM_VALUE_DEPLOYMENT);
    Mockito.when(config.getInitParameter(WroFilter.PARAM_GZIP_RESOURCES)).thenReturn(Boolean.FALSE.toString());
    Mockito.when(config.getInitParameter(WroFilter.PARAM_CACHE_UPDATE_PERIOD)).thenReturn("10");
    Mockito.when(config.getInitParameter(WroFilter.PARAM_MODEL_UPDATE_PERIOD)).thenReturn("100");
    filter.init(config);
    Assert.assertEquals(false, filter.getConfiguration().isDebug());
    Assert.assertEquals(false, filter.getConfiguration().isGzipEnabled());
    Assert.assertEquals(10, filter.getConfiguration().getCacheUpdatePeriod());
    Assert.assertEquals(100, filter.getConfiguration().getModelUpdatePeriod());
	}

	/**
   * Set filter init params with proper values and check they are the same in {@link WroConfiguration} object.
   */
  @Test
  public void testConfigurationInitParam() throws Exception {
    final FilterConfig config = Mockito.mock(FilterConfig.class);
    Mockito.when(config.getInitParameter(WroFilter.PARAM_CONFIGURATION)).thenReturn("anyOtherString");
    filter.init(config);
    Assert.assertEquals(true, filter.getConfiguration().isDebug());
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotProcessConfigResourceStream() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterChain chain = Mockito.mock(FilterChain.class);

    Mockito.when(request.getRequestURI()).thenReturn("");
    filter.doFilter(request, response, chain);
  }

  @Test(expected=WroRuntimeException.class)
  public void cannotProcessInvalidUri() throws Exception {
    requestGroupByUri("");
  }

  @Test
  public void requestValidGroup() throws Exception {
  	requestGroupByUri("/folder/g1.css");
  }

  @Test(expected=InvalidGroupNameException.class)
  public void requestInvalidGroup() throws Exception {
  	requestGroupByUri("/folder/INVALID_GROUP.css");
  }

  @Test(expected=UnauthorizedRequestException.class)
  public void cannotAccessUnauthorizedRequest() throws Exception {
  	final String resourcePath = "/g1.css";
  	final String requestUri = CssUrlRewritingProcessor.PATH_RESOURCES + resourcePath;
		requestGroupByUri(requestUri, new RequestBuilder(requestUri) {
			@Override
			protected HttpServletRequest newRequest() {
				final HttpServletRequest request = super.newRequest();
				Mockito.when(request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID)).thenReturn(resourcePath);
				return request;
			}
		});
  }

  //TODO build model before performing the request
  //@Test
  public void requestUrlRewrittenResource() throws Exception {
  	final String resourcePath = "classpath:ro/isdc/wro/http/2.css";
  	final String requestUri = CssUrlRewritingProcessor.PATH_RESOURCES + "?id=" + resourcePath;
		requestGroupByUri(requestUri, new RequestBuilder(requestUri) {
			@Override
			protected HttpServletRequest newRequest() {
				final HttpServletRequest request = super.newRequest();
				Mockito.when(request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID)).thenReturn(resourcePath);
				return request;
			}
		});
  }

  private void requestGroupByUri(final String requestUri) throws IOException, ServletException {
  	requestGroupByUri(requestUri, new RequestBuilder(requestUri));
  }

  @Test
  public void testDoFilterInDEPLOYMENTMode()
    throws Exception {
    initFilterWithValidConfig();
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(request.getRequestURI()).thenReturn("/g2.js");
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
    Mockito.when(response.getOutputStream()).thenReturn(sos);
    final FilterChain chain = Mockito.mock(FilterChain.class);
    final FilterConfig config = Mockito.mock(FilterConfig.class);
    Mockito.when(config.getInitParameter(WroFilter.PARAM_CONFIGURATION)).thenReturn(WroFilter.PARAM_VALUE_DEPLOYMENT);
    filter.init(config);
    filter.doFilter(request, response, chain);
  }

  /**
   * Perform initialization and simulates a call to WroFilter with given requestUri.
   *
   * @param requestUri
   */
	private void requestGroupByUri(final String requestUri, final RequestBuilder requestBuilder)
		throws IOException, ServletException {
		initFilterWithValidConfig();
    final HttpServletRequest request = requestBuilder.newRequest();
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
    Mockito.when(response.getOutputStream()).thenReturn(sos);
    final FilterChain chain = Mockito.mock(FilterChain.class);
    final FilterConfig config = Mockito.mock(FilterConfig.class);
  	filter.init(config);
    filter.doFilter(request, response, chain);
	}

	class RequestBuilder {
		private final String requestUri;
		public RequestBuilder(final String requestUri) {
			this.requestUri = requestUri;
		}
		protected HttpServletRequest newRequest() {
			final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
			Mockito.when(request.getRequestURI()).thenReturn(requestUri);
			return request;
		}
	}
}
