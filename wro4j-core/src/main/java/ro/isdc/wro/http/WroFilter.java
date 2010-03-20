/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.http;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ConfigurationContext;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;


/**
 * Main entry point. Perform the request processing by identifying the type of the requested resource. Depending on the
 * way it is configured, it builds
 *
 * @author Alex Objelean
 * @created Created on Oct 31, 2008
 */
public class WroFilter
  implements Filter {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WroFilter.class);
  /**
   * The name of the context parameter that specifies wroManager factory class
   */
  static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";
  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode
   * is used.
   */
  static final String PARAM_CONFIGURATION = "configuration";
  /**
   * Deployment configuration option. If false, the DEVELOPMENT (or DEBUG) is assumed.
   */
  static final String PARAM_VALUE_DEPLOYMENT = "DEPLOYMENT";
  /**
   * Gzip resources configuration option.
   */
  static final String PARAM_GZIP_RESOURCES = "gzipResources";
  /**
   * Parameter containing an integer value for specifying how often (in seconds) the cache should be refreshed.
   */
  static final String PARAM_CACHE_UPDATE_PERIOD = "cacheUpdatePeriod";
  /**
   * Parameter containing an integer value for specifying how often (in seconds) the model should be refreshed.
   */
  static final String PARAM_MODEL_UPDATE_PERIOD = "modelUpdatePeriod";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;

  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;
  /**
   * Cache control header values
   */
  private String etagValue;
  private long lastModifiedValue;
  private String cacheControlValue;
  private long expiresValue;

  private WroConfiguration configuration;


  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
    throws ServletException {
    this.filterConfig = config;
    this.wroManagerFactory = getWroManagerFactory();
    initHeaderValues();
    initJMX();
    doInit(config);
  }


  /**
   * Expose MBean to tell JMX infrastructure about our MBean.
   */
  private void initJMX()
    throws ServletException {
    try {
      configuration = newConfiguration();
      ConfigurationContext.get().setConfig(configuration);
      registerChangeListeners();
      final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      final ObjectName name = new ObjectName(WroConfiguration.getObjectName());
      if (!mbs.isRegistered(name)) {
        mbs.registerMBean(configuration, name);
      }
    } catch (final JMException e) {
      LOG.error("Exception occured while registering MBean", e);
    }
  }


  /**
   * Register property change listeners.
   */
  private void registerChangeListeners() {
    configuration.registerCacheUpdatePeriodChangeListener(new PropertyChangeListener() {
    	public void propertyChange(final PropertyChangeEvent evt) {
    	  //reset cache headers when any property is changed in order to avoid browser caching (using ETAG header)
    	  initHeaderValues();
    		if (wroManagerFactory instanceof WroConfigurationChangeListener) {
    			((WroConfigurationChangeListener)wroManagerFactory).onCachePeriodChanged();
    		}
    	}
    });
    configuration.registerModelUpdatePeriodChangeListener(new PropertyChangeListener() {
    	public void propertyChange(final PropertyChangeEvent evt) {
        initHeaderValues();
    		if (wroManagerFactory instanceof WroConfigurationChangeListener) {
    			((WroConfigurationChangeListener)wroManagerFactory).onModelPeriodChanged();
    		}
    	}
    });
  }


  /**
   * Extracts long value from provided init param name configuration.
   */
  private long getUpdatePeriodByName(final String paramName) {
    final String valueAsString = filterConfig.getInitParameter(paramName);
    if (valueAsString == null) {
      return 0;
    }
    try {
      return Long.valueOf(valueAsString);
    } catch (final NumberFormatException e) {
      throw new WroRuntimeException(paramName + " init-param must be a number, but was: " + valueAsString);
    }
  }

  /**
   * @return {@link WroConfiguration} configured object with default values set.
   */
	private WroConfiguration newConfiguration() {
		final WroConfiguration config = new WroConfiguration();

    final String gzipParam = filterConfig.getInitParameter(PARAM_GZIP_RESOURCES);
    final boolean gzipResources = gzipParam == null ? true : Boolean.valueOf(gzipParam);
    config.setGzipEnabled(gzipResources);

    boolean debug = true;
    final String configParam = filterConfig.getInitParameter(PARAM_CONFIGURATION);
    if (configParam != null) {
      if (PARAM_VALUE_DEPLOYMENT.equalsIgnoreCase(configParam)) {
        debug = false;
      }
    }
    config.setDebug(debug);
    config.setCacheUpdatePeriod(getUpdatePeriodByName(PARAM_CACHE_UPDATE_PERIOD));
    config.setModelUpdatePeriod(getUpdatePeriodByName(PARAM_MODEL_UPDATE_PERIOD));
    return config;
	}


	/**
   * Initialize header values used for server-side resource caching.
   */
  private void initHeaderValues() {
    etagValue = UUID.randomUUID().toString();
    lastModifiedValue = new Date().getTime();
    cacheControlValue = "public, max-age=315360000, post-check=315360000, pre-check=315360000";
    final Calendar cal = Calendar.getInstance();
    cal.roll(Calendar.YEAR, 10);
    expiresValue = cal.getTimeInMillis();
  }

  /**
   * Custom filter initialization - can be used for extended classes.
   *
   * @see Filter#init(FilterConfig).
   */
  protected void doInit(final FilterConfig config)
    throws ServletException {}


  /**
   * {@inheritDoc}
   */
  public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
    throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest)req;
    final HttpServletResponse response = (HttpServletResponse)res;
    // add request, response & servletContext to thread local
    Context.set(Context.webContext(request, response, filterConfig));
    if (!ConfigurationContext.get().getConfig().isDebug()) {
      final String ifNoneMatch = request.getHeader(HttpHeader.IF_NONE_MATCH.toString());
      if (etagValue.equals(ifNoneMatch)) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }
    }
    setResponseHeaders(response);
    // process the uri using manager
    wroManagerFactory.getInstance().process(request, response);
    // remove context from the current thread local.
    Context.unset();
  }


  /**
   * Method responsible for setting response headers, used mostly for cache control. Override this method if you want to
   * change the way headers are set.<br>
   * Default implementation will set
   *
   * @param response {@link HttpServletResponse} object.
   */
  protected void setResponseHeaders(final HttpServletResponse response) {
    if (!ConfigurationContext.get().getConfig().isDebug()) {
      // Force resource caching as best as possible
      response.setHeader(HttpHeader.CACHE_CONTROL.toString(), cacheControlValue);
      response.setHeader(HttpHeader.ETAG.toString(), etagValue);
      response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), lastModifiedValue);
      response.setDateHeader(HttpHeader.EXPIRES.toString(), expiresValue);
    }
  }


  /**
   * Factory method for {@link WroManagerFactory}. Override this method, in order to change the way filter use factory.
   *
   * @return {@link WroManagerFactory} object.
   */
  protected WroManagerFactory getWroManagerFactory() {
    final String appFactoryClassName = filterConfig.getInitParameter(PARAM_MANAGER_FACTORY);
    if (appFactoryClassName == null) {
      // If no context param was specified we return the default factory
      return new ServletContextAwareWroManagerFactory();
    } else {
      // Try to find the specified factory class
      Class<?> factoryClass;
      try {
        factoryClass = Thread.currentThread().getContextClassLoader().loadClass(appFactoryClassName);
        // Instantiate the factory
        return (WroManagerFactory)factoryClass.newInstance();
      } catch (final Exception e) {
        throw new WroRuntimeException("Exception while loading WroManagerFactory class", e);
      }
    }
  }


  /**
   * This exists only for testing purposes.
   * @return the applicationSettings
   */
  protected final WroConfiguration getConfiguration() {
    return this.configuration;
  }


  /**
   * {@inheritDoc}
   */
  public void destroy() {
    wroManagerFactory.destroy();
  }

}
