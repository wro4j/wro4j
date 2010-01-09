/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.exception.InvalidGroupNameException;
import ro.isdc.wro.exception.UnauthorizedRequestException;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.impl.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.test.util.WroTestUtils;

/**
 * Test for {@link WroFilter} class.
 *
 * @author Alex Objelean
 * @created Created on Jul 13, 2009
 */
public class TestWroFilter {
	private static final String URL_REWRITING_PREFIX = "/[WRO-PREFIX]" + CssUrlRewritingProcessor.PARAM_RESOURCE_ID
		+ "?=";
	private WroFilter filter;
	@Before
	public void initFilter() throws Exception {
		filter = new WroFilter();
		final FilterConfig config = Mockito.mock(FilterConfig.class);
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

  @Test(expected=WroRuntimeException.class)
  public void cannotProcessInvalidUri() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterChain chain = Mockito.mock(FilterChain.class);

    Mockito.when(request.getRequestURI()).thenReturn("");
    filter.doFilter(request, response, chain);
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
		requestGroupByUri(requestUri, new FilterBuilder(requestUri) {
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
  public void requestUrlRewritternResource() throws Exception {
  	final String resourcePath = "classpath:ro/isdc/wro/http/2.css";
  	final String requestUri = CssUrlRewritingProcessor.PATH_RESOURCES + "?id=" + resourcePath;
		requestGroupByUri(requestUri, new FilterBuilder(requestUri) {
			@Override
			protected HttpServletRequest newRequest() {
				final HttpServletRequest request = super.newRequest();
				Mockito.when(request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID)).thenReturn(resourcePath);
				return request;
			}
		});
  }

  private void requestGroupByUri(final String requestUri) throws IOException, ServletException {
  	requestGroupByUri(requestUri, new FilterBuilder(requestUri));
  }

  /**
   * Perform initialization and simulates a call to WroFilter with given requestUri.
   *
   * @param requestUri
   */
	private void requestGroupByUri(final String requestUri, final FilterBuilder filterBuilder)
		throws IOException, ServletException {
		initFilterWithValidConfig();
    final HttpServletRequest request = filterBuilder.newRequest();
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
    Mockito.when(response.getOutputStream()).thenReturn(sos);
    final FilterChain chain = Mockito.mock(FilterChain.class);
    final FilterConfig config = Mockito.mock(FilterConfig.class);
  	filter.init(config);
    filter.doFilter(request, response, chain);
	}

	class FilterBuilder {
		private final String requestUri;
		public FilterBuilder(final String requestUri) {
			this.requestUri = requestUri;
		}
		protected HttpServletRequest newRequest() {
			final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
			Mockito.when(request.getRequestURI()).thenReturn(requestUri);
			return request;
		}
	}
}
