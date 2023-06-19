/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.http;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.http.ConfigurableWroFilter;
import ro.isdc.wro.manager.factory.WroManagerFactory;

/**
 * A WroFilter capable to get a {@link WroManagerFactory} instance from spring
 * context. The filter must have initParam called <code>targetBeanName</code>
 * whose value will be looked up in the spring application context.
 *
 * @author Alex Objelean
 */
public final class SpringWroFilter extends ConfigurableWroFilter {
  /**
   * Init param for target bean name - used to retrieve factory instance from
   * spring application context.
   */
  private static final String PARAM_TARGET_BEAN_NAME = "targetBeanName";

  /**
   * Default target bean name.
   */
  private static final String DEFAULT_TARGET_BEAN_NAME = "wro4j.wroManagerFactory";

  /**
   * {@link WroManagerFactory} instance from spring application context..
   */
  private WroManagerFactory factory;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doInit(final FilterConfig filterConfig)
      throws ServletException {
    String targetBeanName = filterConfig
        .getInitParameter(PARAM_TARGET_BEAN_NAME);
    // apply default targetBeanName
    targetBeanName = StringUtils.isEmpty(targetBeanName) ? DEFAULT_TARGET_BEAN_NAME
        : targetBeanName;
    final WebApplicationContext ctx = WebApplicationContextUtils
        .getWebApplicationContext(filterConfig.getServletContext());
    factory = (WroManagerFactory) ctx.getBean(targetBeanName,
        WroManagerFactory.class);
    if (factory == null) {
      throw new WroRuntimeException("Could not locate: "
          + WroManagerFactory.class.getName()
          + " instance in applicationContext with bean name: " + targetBeanName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroManagerFactory newWroManagerFactory() {
    return factory;
  }
}
