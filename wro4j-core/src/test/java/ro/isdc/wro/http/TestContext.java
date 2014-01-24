/*
 * Copyright (C) 2009 Wro4j. All rights reserved.
 */
package ro.isdc.wro.http;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;


/**
 * Test Context class behavior.
 * 
 * @author Alex Objelean
 */
public class TestContext {
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotAccessContext() {
    // unset intentionally
    Context.unset();
    Context.get();
  }
  
  @Before
  public void initContext() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Context.set(Context.webContext(request, response, filterConfig));
    // simulate that gzip encoding is accepted
    Mockito.when(Context.get().getRequest().getHeaders(Mockito.anyString())).thenReturn(new Enumeration<String>() {
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
    Context.set(null);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
}
