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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.CacheChangeCallbackAware;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.util.WroUtil;


/**
 * Main entry point. Perform the request processing by identifying the type of the requested resource. Depending on the
 * way it is configured.
 *
 * @author Alex Objelean
 * @created Created on Oct 31, 2008
 */
public class WroFilter
  implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(WroFilter.class);
  /**
   * The prefix to use for default mbean name.
   */
  private static final String MBEAN_PREFIX = "wro4j-";
  /**
   * The parameter used to specify headers to put into the response, used mainly for caching.
   */
  static final String PARAM_HEADER = "header";
  /**
   * The name of the context parameter that specifies wroManager factory class.
   */
  static final String PARAM_MANAGER_FACTORY = "managerFactoryClassName";
  /**
   * Configuration Mode (DEVELOPMENT or DEPLOYMENT) By default DEVELOPMENT mode is used.
   */
  static final String PARAM_CONFIGURATION = "configuration";
  /**
   * Replace with a boolean used for debug Deployment configuration option. If false, the DEVELOPMENT (or DEBUG) is
   * assumed.
   */
  // TODO deprecate and use a boolean value init-param
  static final String PARAM_VALUE_DEPLOYMENT = "DEPLOYMENT";
  /**
   * Gzip resources configuration option.
   */
  static final String PARAM_GZIP_RESOURCES = "gzipResources";
  /**
   * Disable cache configuration option. When true, the processed content won't be cached in DEVELOPMENT mode. In
   * DEPLOYMENT mode changing this flag will have no effect.
   */
  static final String PARAM_DISABLE_CACHE = "disableCache";
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
   * A preferred name of the MBean object.
   */
  static final String PARAM_MBEAN_NAME = "mbeanName";
  /**
   * Default value used by Cache-control header.
   */
  private static final String DEFAULT_CACHE_CONTROL_VALUE = "public, max-age=315360000";
  /**
   * Filter config.
   */
  private FilterConfig filterConfig;
  /**
   * Wro configuration.
   */
  private WroConfiguration wroConfiguration;
  /**
   * WroManagerFactory. The brain of the optimizer.
   */
  private WroManagerFactory wroManagerFactory;
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


    @Override
    public String get(final Object key) {
      return super.get(((String)key).toLowerCase());
    }
  };


  /**
   * {@inheritDoc}
   */
  public final void init(final FilterConfig config)
    throws ServletException {
    this.filterConfig = config;
    wroConfiguration = new WroConfiguration();
    initConfiguration(wroConfiguration);
    initWroManagerFactory();
    initHeaderValues();
    initJMX();
    doInit(config);
  }


  /**
   * Initialize {@link WroManagerFactory}.
   */
  private void initWroManagerFactory() {
    this.wroManagerFactory = getWroManagerFactory();
    if (wroManagerFactory instanceof CacheChangeCallbackAware) {
      // register cache change callback -> when cache is changed, update headers values.
      ((CacheChangeCallbackAware)wroManagerFactory).registerCallback(new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
          // update header values
          initHeaderValues();
        }
      });
    }
//    filterConfig.getServletContext().setAttribute("wro4j:wroManagerFactory", wroManagerFactory);
  }

  /**
   * Expose MBean to tell JMX infrastructure about our MBean.
   */
  private void initJMX() {
    try {
      /**
       * Flag for enable/disable jmx. By default this value is true.
       */
      final boolean jmxEnabled = getJmxEnabled();
      LOG.info("jmxEnabled: " + jmxEnabled);
      registerChangeListeners();
      if (jmxEnabled) {
        final MBeanServer mbeanServer = getMBeanServer();
        final ObjectName name = new ObjectName(newMBeanName(), "type", WroConfiguration.class.getSimpleName());
        if (!mbeanServer.isRegistered(name)) {
          mbeanServer.registerMBean(wroConfiguration, name);
        }
      }
      LOG.info("wro4j configuration: " + wroConfiguration);
    } catch (final JMException e) {
      LOG.error("Exception occured while registering MBean", e);
    }
  }


  /**
   * Override this method for enabling JMX by other means. Treat null value as true.
   *
   * @return flag for enabling JMX.
   */
  protected boolean getJmxEnabled() {
    // TODO do not use BooleanUtils -> create your utility method
    return BooleanUtils.toBooleanDefaultIfNull(
      BooleanUtils.toBooleanObject(filterConfig.getInitParameter(PARAM_JMX_ENABLED)), true);
  }


  /**
   * @return the name of MBean to be used by JMX to configure wro4j.
   */
  protected String newMBeanName() {
    String mbeanName = filterConfig.getInitParameter(PARAM_MBEAN_NAME);
    if (StringUtils.isEmpty(mbeanName)) {
      final String contextPath = getContextPath();
      mbeanName = StringUtils.isEmpty(contextPath) ? "ROOT" : contextPath;
      mbeanName = MBEAN_PREFIX + contextPath;
    }
    return mbeanName;
  }


  /**
   * @return Context path of the application.
   */
  private String getContextPath() {
    String contextPath = null;
    try {
      contextPath = (String)ServletContext.class.getMethod("getContextPath", new Class<?>[] {}).invoke(
        filterConfig.getServletContext(), new Object[] {});
    } catch (final Exception e) {
      contextPath = "DEFAULT";
      LOG.warn("Couldn't identify contextPath because you are using older version of servlet-api (<2.5). Using "
        + contextPath + " contextPath.");
    }
    return contextPath.replaceFirst("/", "");
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
    wroConfiguration.registerCacheUpdatePeriodChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent event) {
        // reset cache headers when any property is changed in order to avoid browser caching
        initHeaderValues();
        if (wroManagerFactory instanceof WroConfigurationChangeListener) {
          ((WroConfigurationChangeListener)wroManagerFactory).onCachePeriodChanged();
        }
      }
    });
    wroConfiguration.registerModelUpdatePeriodChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent event) {
        initHeaderValues();
        if (wroManagerFactory instanceof WroConfigurationChangeListener) {
          ((WroConfigurationChangeListener)wroManagerFactory).onModelPeriodChanged();
        }
      }
    });
    LOG.debug("Cache & Model change listeners were registered");
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
  private void initConfiguration(final WroConfiguration config) {
    config.setGzipEnabled(isGzipResources());
    config.setDebug(isDebug());
    config.setCacheUpdatePeriod(getCacheUpdatePeriod());
    config.setModelUpdatePeriod(getModelUpdatePeriod());
    config.setDisableCache(isDisableCache());
  }


  /**
   * This method can be overriden to set this value differently.
   *
   * @return true if the resources response should be gziped.
   */
  protected boolean isGzipResources() {
    final String gzipParam = filterConfig.getInitParameter(PARAM_GZIP_RESOURCES);
    final boolean gzipResources = gzipParam == null ? true : Boolean.valueOf(gzipParam);
    return gzipResources;
  }

  /**
   * @return flag value configured by filter config init parameter.
   */
  protected boolean isDisableCache() {
    final String paramValue = filterConfig.getInitParameter(PARAM_DISABLE_CACHE);
    final boolean flag = paramValue == null ? false : Boolean.valueOf(paramValue);
    return flag;
  }


  /**
   * This method can be overriden to set this value differently.
   *
   * @return true if the wro4j is run in DEVELOPMENT mode.
   */
  protected boolean isDebug() {
    boolean debug = true;
    final String configParam = filterConfig.getInitParameter(PARAM_CONFIGURATION);
    if (configParam != null) {
      if (PARAM_VALUE_DEPLOYMENT.equalsIgnoreCase(configParam)) {
        debug = false;
      }
    }
    return debug;
  }


  /**
   * This method can be overriden to set this value differently.
   *
   * @return model update period. By default it performs check of init-param.
   */
  protected long getModelUpdatePeriod() {
    return getUpdatePeriodByName(PARAM_MODEL_UPDATE_PERIOD);
  }


  /**
   * This method can be overriden to set this value differently.
   *
   * @return cache update period. By default it performs check of init-param.
   */
  protected long getCacheUpdatePeriod() {
    return getUpdatePeriodByName(PARAM_CACHE_UPDATE_PERIOD);
  }


  /**
   * Initialize header values.
   */
  private void initHeaderValues() {
    // put defaults
    if (!wroConfiguration.isDebug()) {
      final Long timestamp = new Date().getTime();
      final Calendar cal = Calendar.getInstance();
      cal.roll(Calendar.YEAR, 1);
      headersMap.put(HttpHeader.CACHE_CONTROL.toString(), DEFAULT_CACHE_CONTROL_VALUE);
      headersMap.put(HttpHeader.LAST_MODIFIED.toString(), WroUtil.toDateAsString(timestamp));
      headersMap.put(HttpHeader.EXPIRES.toString(), WroUtil.toDateAsString(cal.getTimeInMillis()));
    }
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
          + "<HEADER_NAME1>: <VALUE1> | <HEADER_NAME2>: <VALUE2>. " + "Ex: <look like this: "
          + "Expires: Thu, 15 Apr 2010 20:00:00 GMT | cache-control: public", e);
      }
    }
    LOG.debug("Header Values: " + headersMap);
  }


  /**
   * Parse header value & puts the found values in headersMap field.
   *
   * @param header value to parse.
   */
  private void parseHeader(final String header) {
    LOG.debug("parseHeader: " + header);
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
      Context.set(Context.webContext(request, response, filterConfig), wroConfiguration);
      processRequest(request, response);
      Context.unset();
    } catch (final RuntimeException e) {
      onRuntimeException(e, response, chain);
    }
  }


  /**
   * Perform actual processing.
   */
  private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    setResponseHeaders(response);
    // process the uri using manager
    wroManagerFactory.getInstance().process();
  }


  /**
   * Invoked when a {@link RuntimeException} is thrown. Allows custom exception handling. The default implementation
   * redirects to 404 for a specific {@link WroRuntimeException} exception when in DEPLOYMENT mode.
   *
   * @param e {@link RuntimeException} thrown during request processing.
   */
  protected void onRuntimeException(final RuntimeException e, final HttpServletResponse response,
    final FilterChain chain) {
    LOG.debug("RuntimeException occured", e);
    try {
      LOG.debug("Cannot process. Proceeding with chain execution.");
      chain.doFilter(Context.get().getRequest(), response);
    } catch (final Exception ex) {
      // should never happen
      LOG.error("Error while chaining the request: " + HttpServletResponse.SC_NOT_FOUND);
    }
  }


  /**
   * Method called for each request and responsible for setting response headers, used mostly for cache control.
   * Override this method if you want to change the way headers are set.<br>
   *
   * @param response {@link HttpServletResponse} object.
   */
  protected void setResponseHeaders(final HttpServletResponse response) {
    // Force resource caching as best as possible
    for (final Map.Entry<String, String> entry : headersMap.entrySet()) {
      response.setHeader(entry.getKey(), entry.getValue());
    }
    //prevent caching when in development mode
    if (wroConfiguration.isDebug()) {
      WroUtil.addNoCacheHeaders(response);
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
      Class<?> factoryClass = null;
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
   * @return the {@link WroConfiguration} associated with this filter instance.
   */
  public final WroConfiguration getWroConfiguration() {
    return this.wroConfiguration;
  }


  /**
   * {@inheritDoc}
   */
  public void destroy() {
    wroConfiguration.destroy();
    Context.destroy();
    wroManagerFactory.destroy();
  }
}
