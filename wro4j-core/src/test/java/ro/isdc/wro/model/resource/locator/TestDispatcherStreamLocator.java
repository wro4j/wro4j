/*
 * Copyright (C) 2011. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


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

  @Test
  public void shouldLocateRelativeResource()
      throws Exception {
    locator = new DispatcherStreamLocator() {
      @Override
      protected UriLocator newExternalUriLocator() {
        return new UrlUriLocator() {
          @Override
          public InputStream locate(String uri)
              throws IOException {
            return new ByteArrayInputStream("some content".getBytes());
          }
        };
      }
    };
    Assert.assertNotNull(locator.getInputStream(mockRequest, mockResponse, "/static/relative.js"));
  }
}
