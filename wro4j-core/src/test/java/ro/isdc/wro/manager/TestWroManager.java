/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.http.Context;
import ro.isdc.wro.manager.impl.StandAloneWroManagerFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;

/**
 * TestWroManager.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestWroManager {
	@Before
	public void setUp() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    Context.set(new Context(request, response, filterConfig));
	}
  @Test
  public void first() throws IOException {
    final WroManagerFactory factory = new StandAloneWroManagerFactory();
    final WroManager manager = factory.getInstance();
    manager.setModelFactory(new XmlModelFactory() {
    	@Override
    	protected InputStream getConfigResourceAsStream() {
    		return getResourceAsStream(TestWroManager.class.getPackage().getName().replace(".", "/") + "/wro.xml");
    	}
    });

    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");
    final WroProcessResult result = manager.process(request);
//		final Writer writer = new StringWriter();
//		IOUtils.copy(result.getInputStream(), writer);
//		System.out.println("Processing result: " + writer.toString());
  }
}
