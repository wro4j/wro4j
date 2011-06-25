/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.examples.guice;

import java.util.Map;

import javax.servlet.FilterConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.directwebremoting.servlet.DwrServlet;

import ro.isdc.wro.examples.WebResourceOptimizationApplication;
import ro.isdc.wro.http.WroFilter;

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

/**
 * @author Alex Objelean
 */
public class WroGuiceListener extends GuiceServletContextListener {
  /**
   * {@inheritDoc}
   */
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new ServletModule() {
      @Override
      protected void configureServlets() {
        //bindings
        bind(WroFilter.class).in(Singleton.class);
        bind(WicketFilter.class).in(Singleton.class);
        bind(DwrServlet.class).in(Singleton.class);

        //filters
        //find out how to add dispatchers to the filter mapping configuration
        filter("/wro/*").through(WroFilter.class);


        final String wicketFilterPath = "/*";
        final Map<String, String> wicketFilterMap = Maps.newHashMap();
        wicketFilterMap.put(ContextParamWebApplicationFactory.APP_CLASS_PARAM,
          WebResourceOptimizationApplication.class.getName());
        filter(wicketFilterPath).through(new WicketFilter() {
          @Override
          protected String getFilterPathFromConfig(final FilterConfig filterConfig) {
            return StringUtils.EMPTY;
          }
        }, wicketFilterMap);
        //servlets
        serve("/dwr/*").with(DwrServlet.class);
      }
    });
  }

}
