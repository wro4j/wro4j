/*
 * Copyright (C) 2009 Wro4j.
 * All rights reserved.
 */
package ro.isdc.wro.http;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		Context.get();
	}

	@Test
	public void testIsGzipEnabled() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
		Context.set(new Context(request, response, filterConfig));
		Mockito.when(request.getHeaders(Mockito.anyString())).thenReturn(new Enumeration<String>() {
			public boolean hasMoreElements() {
				return false;
			}
			public String nextElement() {
				return null;
			}
		});
		Context.get().isGzipEnabled();
		Context.get().isDevelopmentMode();
	}
}
