/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.http.Context;
import ro.isdc.wro.manager.impl.StandAloneWroManagerFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;

/**
 * TestWroManager.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @created Created on Nov 3, 2008
 */
public class TestWroManager {
	@Before
	public void setUp() {
		final Context context = Mockito.mock(Context.class);
		Mockito.when(context.isDevelopmentMode()).thenReturn(true);
		Context.set(context);
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


    final String uri = "/app/g1.css";
//    final WroProcessResult result = manager.process(uri);
//		final Writer writer = new StringWriter();
//		IOUtils.copy(result.getInputStream(), writer);
//		System.out.println("Processing result: " + writer.toString());
  }
}
