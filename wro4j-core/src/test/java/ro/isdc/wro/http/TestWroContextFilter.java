/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.config.DefaultContext;


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

  @Before
  public void setUp()
    throws Exception {
    MockitoAnnotations.initMocks(this);
    victim = new WroContextFilter();
    Mockito.when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
    victim.init(mockFilterConfig);
  }

  @Test
  public void shouldInitializeContextForChainedFilters() throws Exception {
    Mockito.doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        Assert.assertTrue(DefaultContext.isContextSet());
        return null;
      }
    }).when(mockFilterChain).doFilter(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    victim.doFilter(mockRequest, mockResponse, mockFilterChain);
    Mockito.verify(mockFilterChain, Mockito.times(1)).doFilter(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
    //After chain processing, the context must be unset
    Assert.assertFalse(DefaultContext.isContextSet());
  }
}
