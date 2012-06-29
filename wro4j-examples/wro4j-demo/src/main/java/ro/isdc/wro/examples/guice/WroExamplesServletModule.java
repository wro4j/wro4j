package ro.isdc.wro.examples.guice;

import java.util.Map;

import javax.servlet.FilterConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.directwebremoting.servlet.DwrServlet;

import ro.isdc.wro.examples.ExternalResourceServlet;
import ro.isdc.wro.examples.WebResourceOptimizationApplication;
import ro.isdc.wro.examples.http.DispatchResourceServlet;
import ro.isdc.wro.examples.http.DynamicResourceServlet;
import ro.isdc.wro.examples.http.RedirectResourceServlet;
import ro.isdc.wro.examples.http.WroFilterWithCustomRequestHandlers;
import ro.isdc.wro.extensions.http.CoffeeScriptFilter;
import ro.isdc.wro.extensions.http.LessCssFilter;
import ro.isdc.wro.http.WroContextFilter;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

/**
 * @author Alex Objelean
 */
final class WroExamplesServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    //bindings
    bind(WroFilterWithCustomRequestHandlers.class).in(Singleton.class);
    bind(WicketFilter.class).in(Singleton.class);
    bind(DwrServlet.class).in(Singleton.class);
    bind(ExternalResourceServlet.class).in(Singleton.class);
    bind(DynamicResourceServlet.class).in(Singleton.class);
    bind(RedirectResourceServlet.class).in(Singleton.class);
    bind(DispatchResourceServlet.class).in(Singleton.class);
    bind(LessCssFilter.class).in(Singleton.class);
    bind(CoffeeScriptFilter.class).in(Singleton.class);
    bind(WroContextFilter.class).in(Singleton.class);

    // filters
    // TODO find out how to add dispatchers to the filter mapping configuration
    filter("/wro/*").through(WroFilterWithCustomRequestHandlers.class);
    
    filter("/*").through(WroContextFilter.class);
    wicketFilter("/*");
    filter("*.less").through(LessCssFilter.class);
    filter("*.coffee").through(CoffeeScriptFilter.class);

    //servlets
    serve("/dwr/*").with(DwrServlet.class);
    serve("/external/*").with(ExternalResourceServlet.class);
    serve("/resource/dynamic.js").with(DynamicResourceServlet.class);
    serve("/resource/redirect.js").with(RedirectResourceServlet.class);
    serve("/resource/dispatch.js").with(DispatchResourceServlet.class);
    
  }

  /**
   * Prepare the wicket filter
   */
  protected void wicketFilter(final String wicketFilterPath) {
    final Map<String, String> wicketFilterMap = Maps.newHashMap();
    wicketFilterMap.put(ContextParamWebApplicationFactory.APP_CLASS_PARAM,
      WebResourceOptimizationApplication.class.getName());
    filter(wicketFilterPath).through(new WicketFilter() {
      @Override
      protected String getFilterPathFromConfig(final FilterConfig filterConfig) {
        return StringUtils.EMPTY;
      }
    }, wicketFilterMap);
  }
}