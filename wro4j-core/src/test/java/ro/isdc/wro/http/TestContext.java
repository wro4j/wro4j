/*
 * Copyright (C) 2009 Wro4j. All rights reserved.
 */
package ro.isdc.wro.http;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.DefaultContext;


/**
 * Test Context class behavior.
 *
 * @author Alex Objelean
 */
public class TestContext {
  @Test(expected = WroRuntimeException.class)
  public void cannotAccessContext() {
    // unset intentionally
    DefaultContext.unset();
    DefaultContext.get();
  }


  @Before
  public void initContext() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    DefaultContext.set(DefaultContext.webContext(request, response, filterConfig));
    // simulate that gzip encoding is accepted
    Mockito.when(DefaultContext.get()
      .getRequest()
      .getHeaders(Mockito.anyString()))
      .thenReturn(new Enumeration<String>() {
        public boolean hasMoreElements() {
          return true;
        }


        public String nextElement() {
          return "gzip";
        }
      });
  }


  @Test(expected = NullPointerException.class)
  public void cannotSetNullContext() {
    DefaultContext.set(null);
  }


  @After
  public void tearDown() {
    DefaultContext.unset();
  }

  // @Test
  // public void testGzipParamIsEnabled() {
  // Mockito.when(Context.get().getRequest().getParameter(Context.PARAM_GZIP)).thenReturn("true");
  // Assert.assertTrue(Context.get().isGzipEnabled());
  // }
  //
  // @Test
  // public void testGzipParamIsNotEnabled() {
  // Mockito.when(Context.get().getRequest().getParameter(Context.PARAM_GZIP)).thenReturn("false");
  // Assert.assertFalse(Context.get().isGzipEnabled());
  // }
}
