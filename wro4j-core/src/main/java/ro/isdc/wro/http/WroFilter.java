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
import java.util.LinkedHashMap;
import java.util.Map;

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

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ConfigurationContext;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.CacheChangeCallbackAware;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.util.WroUtil;


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
   * The parameter used to specify headers to put into the response, used mostly for caching.
   */
  static final String PARAM_HEADER = "header";
  /**
   * The name of the context parameter that specifies wroManager factory class
   */
  static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";
  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode is used.
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
   * Parameter allowing to turn jmx on or off.
   */
  static final String PARAM_JMX_ENABLED = "jmxEnabled";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;

  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;
  private WroConfiguration configuration;
  /**
   * Flag for enable/disable jmx. By default this value is true.
   */
  private boolean jmxEnabled = true;
  /**
   * Map containing header values used to control caching. The keys from this values are trimmed and lower-cased when
   * put, in order to avoid duplicate keys. This is done, because according to RFC 2616 Message Headers field names are
   * case-insensitive.
   */
  @SuppressWarnings("serial")
  private final Map<String, String> headersMap = new LinkedHashMap<String, String>() {
    @Override
    public String put(final String key, final String value) {
      return super.put(key.trim().toLowerCase(), value);
    }
    public String get(final Object key) {
      return super.get(((String) key).toLowerCase());
    }
  };


  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
    throws ServletException {
    this.filterConfig = config;
    initWroManagerFactory();
    initHeaderValues();
    doInit(config);
    initJMX();
  }


  /**
   * Initialize {@link WroManagerFactory}.
   */
  private void initWroManagerFactory() {
    this.wroManagerFactory = getWroManagerFactory();
    if (wroManagerFactory instanceof CacheChangeCallbackAware) {
      ((CacheChangeCallbackAware)wroManagerFactory).registerCallback(new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
          //update header values
          initHeaderValues();
        }
      });
    }
  }


  /**
   * Expose MBean to tell JMX infrastructure about our MBean.
   */
  private void initJMX()
    throws ServletException {
    try {
      // treat null as true
      //TODO do not use BooleanUtils -> create your utility method
      jmxEnabled = BooleanUtils.toBooleanDefaultIfNull(
        BooleanUtils.toBooleanObject(filterConfig.getInitParameter(PARAM_JMX_ENABLED)), true);
      configuration = newConfiguration();
      ConfigurationContext.get().setConfig(configuration);
      LOG.debug("jmxEnabled: " + jmxEnabled);
      LOG.debug("wro4j configuration: " + configuration);
      if (jmxEnabled) {
        registerChangeListeners();
        final MBeanServer mbeanServer = getMBeanServer();
        final ObjectName name = new ObjectName(WroConfiguration.getObjectName());
        if (!mbeanServer.isRegistered(name)) {
          mbeanServer.registerMBean(configuration, name);
        }
      }
    } catch (final JMException e) {
      LOG.error("Exception occured while registering MBean", e);
    }
  }


  /**
   * Override this method if you want to provide a different MBeanServer.
   *
   * @return {@link MBeanServer} to use for JMX.
   */
  protected MBeanServer getMBeanServer() {
    return ManagementFactory.getPlatformMBeanServer();
  }


  /**
   * Register property change listeners.
   */
  private void registerChangeListeners() {
    configuration.registerCacheUpdatePeriodChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent event) {
        // reset cache headers when any property is changed in order to avoid browser caching (using ETAG header)
        initHeaderValues();
        if (wroManagerFactory instanceof WroConfigurationChangeListener) {
          ((WroConfigurationChangeListener)wroManagerFactory).onCachePeriodChanged();
        }
      }
    });
    configuration.registerModelUpdatePeriodChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent event) {
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
   * Initialize header values.
   */
  private void initHeaderValues() {
    // put defaults
    final Long timestamp = new Date().getTime();
    final Calendar cal = Calendar.getInstance();
    cal.roll(Calendar.YEAR, 10);

    headersMap.put(HttpHeader.CACHE_CONTROL.toString(),
      "public, max-age=315360000, post-check=315360000, pre-check=315360000");
    headersMap.put(HttpHeader.ETAG.toString(), Long.toHexString(timestamp));
    headersMap.put(HttpHeader.LAST_MODIFIED.toString(), WroUtil.toDateAsString(timestamp));
    headersMap.put(HttpHeader.EXPIRES.toString(), WroUtil.toDateAsString(cal.getTimeInMillis()));

    final String headerParam = filterConfig.getInitParameter(PARAM_HEADER);
    if (headerParam != null) {
      try {
        if (headerParam.contains("|")) {
          final String[] headers = headerParam.split("[|]");
          for (final String header : headers) {
            parseHeader(header);
          }
        } else {
          parseHeader(headerParam);
        }
      } catch (final Exception e) {
        throw new WroRuntimeException("Invalid header init-param value: " + headerParam
          + ". A correct value should have the following format: "
          + "<HEADER_NAME1>: <VALUE1> | <HEADER_NAME2>: <VALUE2>. " + "Ex: <look like this: " + "Expires: Thu, 15 Apr 2010 20:00:00 GMT | ETag: 123456789", e);
      }
    }
    LOG.info("Header Values :" + headersMap);
  }

  /**
   * Parse header value & puts the found values in headersMap field.
   *
   * @param header value to parse.
   */
  private void parseHeader(final String header) {
    final String headerName = header.substring(0, header.indexOf(":"));
    if (!headersMap.containsKey(headerName)) {
      headersMap.put(headerName, header.substring(header.indexOf(":") + 1));
    }
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
    try {
      // add request, response & servletContext to thread local
      Context.set(Context.webContext(request, response, filterConfig));
      if (!ConfigurationContext.get().getConfig().isDebug()) {
        final String ifNoneMatch = request.getHeader(HttpHeader.IF_NONE_MATCH.toString());
        final String etagValue = headersMap.get(HttpHeader.ETAG.toString());
        LOG.info("Request ETag: " + ifNoneMatch);
        LOG.info("Resource ETag: " + etagValue);
        if (etagValue != null && etagValue.equals(ifNoneMatch)) {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }
      }
      setResponseHeaders(response);
      // process the uri using manager
      wroManagerFactory.getInstance().process(request, response);
      // remove context from the current thread local.
      Context.unset();
    } catch (final RuntimeException e) {
      onRuntimeException(e, response);
    }
  }

  /**
   * Invoked when a {@link RuntimeException} is thrown. Allows custom exception handling. By default the exception is thrown further.
   *
   * @param e {@link RuntimeException}.
   */
  protected void onRuntimeException(final RuntimeException e, final HttpServletResponse response) {
    throw e;
  }


  /**
   * Method called for each request and responsible for setting response headers, used mostly for cache control.
   * Override this method if you want to change the way headers are set.<br>
   *
   * @param response {@link HttpServletResponse} object.
   */
  protected void setResponseHeaders(final HttpServletResponse response) {
    if (!ConfigurationContext.get().getConfig().isDebug()) {
      // Force resource caching as best as possible
      for (final Map.Entry<String, String> entry : headersMap.entrySet()) {
        response.setHeader(entry.getKey(), entry.getValue());
      }
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
   *
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
