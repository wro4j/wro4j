/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.config.DefaultContext;


/**
 * @author Alex Objelean
 */
public class TestDispatcherStreamLocator {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private RequestDispatcher mockDispatcher;
  @Mock
  private HttpServletResponse mockResponse;
  private DispatcherStreamLocator locator;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("/resource.js"));
    Mockito.when(mockRequest.getServletPath()).thenReturn("");
    DefaultContext.set(DefaultContext.standaloneContext());
    locator = new DispatcherStreamLocator();
  }

  @Test(expected = NullPointerException.class)
  public void shouldNotAcceptNullRequestOrResponse()
      throws Exception {
    locator.getInputStream(null, null, null);
  }

  @Test(expected = IOException.class)
  public void cannotLocateNullLocation()
      throws Exception {
    locator.getInputStream(mockRequest, mockResponse, null);
  }

  @Test(expected = IOException.class)
  public void cannotLocateInvalidLocation()
      throws Exception {
    locator.getInputStream(mockRequest, mockResponse, "/INVALID");
  }

  @Test
  public void canLocateValidResource()
      throws Exception {
    Mockito.when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(""));
    Assert.assertNotNull(locator.getInputStream(mockRequest, mockResponse, "http://www.google.com"));
  }

  @Test(expected=IOException.class)
  public void testDispatchIncludeHasNoValidResource()
      throws Exception {
    Mockito.when(mockRequest.getRequestDispatcher(Mockito.anyString())).thenReturn(mockDispatcher);
    Mockito.doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        throw new IOException("Include doesn't work... nothing found");
      }
    }).when(mockDispatcher).include(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    locator.getInputStream(mockRequest, mockResponse, "/static/*.js");
  }

  @Test
  public void testDispatchIncludeReturnsValidResource()
      throws Exception {
    Mockito.when(mockRequest.getRequestDispatcher(Mockito.anyString())).thenReturn(mockDispatcher);
    Mockito.doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        //do nothing
        return null;
      }
    }).when(mockDispatcher).include(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    Assert.assertNotNull(locator.getInputStream(mockRequest, mockResponse, "/static/*.js"));
  }
}
