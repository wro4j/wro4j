/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Alex Objelean
 * @created Created on Jul 25, 2010
 */
public class TestConfigurableWroFilter {
  @Test
  public void testFilterWithoutInitParam() throws Exception {
    final Filter filter = new ConfigurableWroFilter();
    filter.init(Mockito.mock(FilterConfig.class));
    filter.doFilter(Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class), new FilterChain() {
      public void doFilter(final ServletRequest arg0, final ServletResponse arg1)
        throws IOException, ServletException {

      }
    });
    //TODO update configuration
//    final ApplicationContext ctx = new ClassPathXmlApplicationContext(
//        "wro4j-extensions-applicationContext.xml");
//    final WroManagerFactory factory = (WroManagerFactory) ctx.getBean(
//        "wro4j.wroManagerFactory", WroManagerFactory.class);
//    factory.getInstance();
  }
}
