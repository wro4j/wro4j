/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;


/**
 * Test for {@link WroFilter} class.
 *
 * @author Alex Objelean
 * @created Created on Jul 13, 2009
 */
public class TestWroContextFilter {
  private WroContextFilter victim;
  @Mock
  private FilterConfig mockFilterConfig;
  @Mock
  private ServletContext mockServletContext;
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  @Mock
  private FilterChain mockFilterChain;
  @Mock
  private ServletContextAttributeHelper mockServletContextAttributeHelper;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp()
    throws Exception {
    MockitoAnnotations.initMocks(this);
    victim = new WroContextFilter() {
      @Override
      ServletContextAttributeHelper getServletContextAttributeHelper() {
        return mockServletContextAttributeHelper;
      }
    };
    when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    victim.init(mockFilterConfig);
  }

  @Test
  public void shouldInitializeContextForChainedFilters() throws Exception {
    doAnswer(new Answer<Void>() {
      public Void answer(InvocationOnMock invocation)
          throws Throwable {
        assertTrue(Context.isContextSet());
        return null;
      }
    }).when(mockFilterChain).doFilter(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verify(mockFilterChain, times(1)).doFilter(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
    //After chain processing, the context must be unset
    assertFalse(Context.isContextSet());
  }
  
  @Test
  public void shouldUseWroConfigurationFoundInServletContext() throws Exception {
    final WroConfiguration config = new WroConfiguration();
    config.setCacheUpdatePeriod(1000);
    
    when(mockServletContextAttributeHelper.getWroConfiguration()).thenReturn(config);
    doAnswer(new Answer<Void>() {
      public Void answer(InvocationOnMock invocation)
          throws Throwable {
        assertSame(config, Context.get().getConfig());
        return null;
      }
    }).when(mockFilterChain).doFilter(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    verify(mockFilterChain, Mockito.times(1)).doFilter(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
  }
  
  /**
   * Test for issue824 (https://code.google.com/p/wro4j/issues/detail?id=824).
   * Checks that nested requests are properly cleaned up. 
   */
  @Test
  public void shouldNotLeakContextForNestedRequest()
      throws Exception {
    
    final WroFilter innerFilter = new WroFilter();
    innerFilter.init(mockFilterConfig);

    final FilterChain chain = new FilterChain() {
      public void doFilter(ServletRequest req, ServletResponse res)
          throws IOException, ServletException {
        victim.doFilter(req, res, mockFilterChain);
      }
    };
    
    victim.init(mockFilterConfig);
    victim.doFilter(mockRequest, mockResponse, chain);
    assertEquals(0, Context.countActive());
  }
}
