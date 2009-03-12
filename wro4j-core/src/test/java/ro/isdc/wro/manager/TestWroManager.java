/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.IOException;

import org.junit.Test;

import ro.isdc.wro.manager.impl.StandAloneWroManagerFactory;

/**
 * TestWroManager.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class TestWroManager {
  @Test
  public void first() throws IOException {
    final WroManagerFactory factory = new StandAloneWroManagerFactory();
    final WroManager manager = factory.getInstance();
    final String uri = "/app/g1.css";
    // final WroProcessResult result = manager.process(uri);
    // final Writer writer = new StringWriter();
    // IOUtils.copy(result.getInputStream(), writer);
    // System.out.println("Processing result: " + writer.toString());
  }
}
