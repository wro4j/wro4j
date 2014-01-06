/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.util.WroTestUtils;


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
  @Mock
  private UriLocator mockUriLocator;
  private DispatcherStreamLocator victim;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("/resource.js"));
    Mockito.when(mockRequest.getServletPath()).thenReturn("");
    Context.set(Context.standaloneContext());
    victim = new DispatcherStreamLocator();
    WroTestUtils.createInjector().inject(victim);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected = IOException.class)
  public void shouldNotAcceptNullRequestOrResponse()
      throws Exception {
    victim.getInputStream(null, null, null);
  }

  @Test(expected = IOException.class)
  public void cannotLocateNullLocation()
      throws Exception {
    victim.getInputStream(mockRequest, mockResponse, null);
  }

  @Test(expected = IOException.class)
  public void cannotLocateInvalidLocation()
      throws Exception {
    victim.getInputStream(mockRequest, mockResponse, "/INVALID");
  }

  @Test
  public void canLocateValidResource()
      throws Exception {
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(""));
    assertNotNull(victim.getInputStream(mockRequest, mockResponse, "http://www.google.com"));
  }

  @Test(expected = IOException.class)
  public void testDispatchIncludeHasNoValidResource()
      throws Exception {
    when(mockRequest.getRequestDispatcher(Mockito.anyString())).thenReturn(mockDispatcher);
    doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        throw new IOException("Include doesn't work... nothing found");
      }
    }).when(mockDispatcher).include(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    victim.getInputStream(mockRequest, mockResponse, "/static/*.js");
  }

  @Test
  public void shouldReturnsResourceIncludedByDispatcher()
      throws Exception {
    final String content = "SomeNonEmptyContent";
    when(mockRequest.getRequestDispatcher(Mockito.anyString())).thenReturn(mockDispatcher);
    doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        final HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
        response.getOutputStream().write(content.getBytes());
        return null;
      }
    }).when(mockDispatcher).include(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
    assertEquals(content, IOUtils.toString(victim.getInputStream(mockRequest, mockResponse, "/static/*.js")));
    verify(mockUriLocator, never()).locate(Mockito.anyString());
  }

  @Test
  public void shouldFallbackToExternalResourceLocatorWhenDispatcherReturns404() throws Exception {
    victim = new DispatcherStreamLocator() {
      @Override
      UriLocator createExternalResourceLocator() {
        return mockUriLocator;
      }
    };
    final String location ="/some/location.js";
    when(mockRequest.getRequestDispatcher(location)).thenReturn(mockDispatcher);
    Mockito.doAnswer(new Answer<Void>() {
      public Void answer(final InvocationOnMock invocation)
          throws Throwable {
        //simulate the dispatched response is empty
        return null;
      }
    }).when(mockDispatcher).include(Mockito.any(HttpServletRequest.class),
        Mockito.any(HttpServletResponse.class));
    victim.getInputStream(mockRequest, mockResponse, location);

    verify(mockUriLocator).locate(Mockito.anyString());
  }

  @Test(expected = NullPointerException.class)
  public void cannotCheckNullRequestAsIncluded() {
    DispatcherStreamLocator.isIncludedRequest(null);
  }

  @Test
  public void shouldNotBeIncludedRequestByDefault() {
    assertFalse(DispatcherStreamLocator.isIncludedRequest(mockRequest));
  }

  @Test
  public void shouldMarkAsIncludedTheRequestWhenDispatcherIsUsed() throws Exception {
    shouldReturnsResourceIncludedByDispatcher();
    verify(mockRequest).setAttribute(DispatcherStreamLocator.ATTRIBUTE_INCLUDED_BY_DISPATCHER, Boolean.TRUE);
  }
}
