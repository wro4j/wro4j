/*
 * Copyright (C) 2009 Wro4j.
 * All rights reserved.
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

import ro.isdc.wro.exception.WroRuntimeException;

/**
 * Test Context class behavior.
 *
 * @author Alex Objelean
 */
public class TestContext {
	@Test(expected=WroRuntimeException.class)
	public void cannotAccessContext() {
	  //unset intentionally
	  Context.unset();
	  Context.get();
	}

	@Before
	public void initContext() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Context.set(new Context(request, response, filterConfig));
    //simulate that gzip encoding is accepted
    Mockito.when(Context.get().getRequest().getHeaders(Mockito.anyString())).thenReturn(new Enumeration<String>() {
      public boolean hasMoreElements() {
        return true;
      }
      public String nextElement() {
        return "gzip";
      }
    });
	}

	@After
	public void tearDown() {
	  Context.unset();
	}

//	@Test
//	public void testGzipParamIsEnabled() {
//	  Mockito.when(Context.get().getRequest().getParameter(Context.PARAM_GZIP)).thenReturn("true");
//		Assert.assertTrue(Context.get().isGzipEnabled());
//	}
//
//	@Test
//  public void testGzipParamIsNotEnabled() {
//    Mockito.when(Context.get().getRequest().getParameter(Context.PARAM_GZIP)).thenReturn("false");
//    Assert.assertFalse(Context.get().isGzipEnabled());
//  }
}
