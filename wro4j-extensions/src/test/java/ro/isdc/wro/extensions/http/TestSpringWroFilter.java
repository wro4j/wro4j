/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.http;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ro.isdc.wro.manager.WroManagerFactory;

/**
 * TestSpringWroFilter.java.
 *
 * @author Alex Objelean
 * @created Created on Dec 5, 2008
 */
public class TestSpringWroFilter {
  @Test
  public void testFilterWithoutInitParam() {
    final ApplicationContext ctx = new ClassPathXmlApplicationContext(
        "wro4j-extensions-applicationContext.xml");
    final WroManagerFactory factory = (WroManagerFactory) ctx.getBean(
        "wro4j.wroManagerFactory", WroManagerFactory.class);
    factory.getInstance();
  }
}
