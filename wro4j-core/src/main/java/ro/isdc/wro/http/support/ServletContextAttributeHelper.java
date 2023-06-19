package ro.isdc.wro.http.support;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.http.WroServletContextListener;
import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * <p>Encapsulates the details of storing/retrieving attributes from {@link ServletContext}. <br/>
 * This class know how to handle multiple {@link WroFilter} and {@link WroServletContextListener} configurations, by
 * setting the attributes with a unique key computed based on provided name (using
 * {@link ServletContextAttributeHelper#getName()}. Use this class to retrieve wro4j related attributes, instead of
 * hard-coding the name of the attribute to retrieve.</p>
 *
 * <p>Usage scenario:</p>
 * <ul>
 * <li>This is the most common use-case, when there is a single {@link WroFilter} and a
 * {@link WroServletContextListener} configured in web.xml. In order to retrieve/store attributes in servlet context
 * use:
 * 
 * <pre>
 * {@link ServletContextAttributeHelper} helper = new {@link ServletContextAttributeHelper}(servletContext);
 * {@link WroConfiguration} config = helper.getWroConfiguration();
 * {@link WroManagerFactory} managerFactory = helper.getManagerFactory();
 * </pre>
 * 
 * </li>
 * <li>When there are multiple {@link WroFilter} configurations in web.xml (each should have a dedicated listener
 * extended form {@link WroServletContextListener}). In order to retrieve/store attributes in servlet context use:
 * 
 * <pre>
 *       String name = ..//the name of the filter (defined in init-param called name). 
 *       {@link ServletContextAttributeHelper} helper = new {@link ServletContextAttributeHelper}(servletContext, name);
 *       {@link WroConfiguration} config = helper.getWroConfiguration();
 *       {@link WroManagerFactory} managerFactory = helper.getManagerFactory();
 * </pre>
 * 
 * </li>
 * <li>When there are multiple {@link WroFilter} configurations in web.xml, you can retrieve/store attributes from
 * within a filter using:
 * 
 * <pre>
 * {@link ServletContextAttributeHelper} helper = {@link ServletContextAttributeHelper}.create(filterConfig);
 * {@link WroConfiguration} config = helper.getWroConfiguration();
 * {@link WroManagerFactory} managerFactory = helper.getManagerFactory();
 * </pre>
 * 
 * This example is similar to previous, except that the name of the filter will be extracted in the factory method.</li>
 * </ul>
 * 
 * @author Alex Objelean
 * @since 1.4.6
 */
public class ServletContextAttributeHelper {
  private static final Logger LOG = LoggerFactory.getLogger(WroServletContextListener.class);
  /**
   * The name of the init param used to retrieve the name of the filter. This is useful when you want to have multiple
   * filter declarations and a lister (of {@link WroServletContextListener} type) associated for each filter. This way
   * you can ensure that each filter will use its own configurations.
   * @VisibleForTesting
   */
  static final String INIT_PARAM_NAME = "name";
  /**
   * Default value of the name init param. This one is used when no value is defined.
   */
  public static final String DEFAULT_NAME = "default";
  /**
   * {@link ServletContext} where the attributes are stored.
   */
  private ServletContext servletContext;
  /**
   * Unique name associated with servletContext, required for multiple listener support.
   */
  private String name;
  
  /**
   * Supported attributes.
   * @VisibleForTesting
   */
  static enum Attribute {
    CONFIGURATION(WroConfiguration.class), MANAGER_FACTORY(WroManagerFactory.class);
    private Class<?> type;
    
    private Attribute(final Class<?> type) {
      this.type = type;
    }
    
    /**
     * @return true if the object is a valid subtype.
     */
    boolean isValid(final Object object) {
      return object == null || type.isAssignableFrom(object.getClass());
    }
  }
  
  /**
   * Uses default name for storing/retrieving attributes in {@link ServletContext}.
   */
  public ServletContextAttributeHelper(final ServletContext servletContext) {
    this(servletContext, DEFAULT_NAME);
  }
  
  /**
   * Factory method which uses default name for storing/retrieving attributes in {@link ServletContext}.
   */
  public static ServletContextAttributeHelper create(final FilterConfig filterConfig) {
    Validate.notNull(filterConfig);
    final String nameFromParam = filterConfig.getInitParameter(INIT_PARAM_NAME);
    final String name = nameFromParam != null ? nameFromParam : DEFAULT_NAME;
    return new ServletContextAttributeHelper(filterConfig.getServletContext(), name);
  }
  
  /**
   * Uses default name to work with {@link ServletContext} attribute.
   * 
   * @param servletContext
   *          a not null {@link ServletContext} object.
   * @param name
   *          of the listener for which the helper will be used.
   */
  public ServletContextAttributeHelper(final ServletContext servletContext, final String name) {
    Validate.notNull(servletContext);
    Validate.notBlank(name);
    this.servletContext = servletContext;
    this.name = name;
    LOG.debug("initializing attributeHelper named: {}", name);
  }
  
  /**
   * Creates a unique name used as a key to store the attribute in {@link ServletContext}.
   * 
   * @param attribute
   *          type of attribute.
   * @return the name of the attribute used to store in servlet context.
   * @VisibleForTesting
   */
  final String getAttributeName(final Attribute attribute) {
    Validate.notNull(attribute);
    return WroServletContextListener.class.getName() + "-" + attribute.name() + "-" + this.name;
  }
  
  /**
   * @return the value of the attribute stored in {@link ServletContext} for this listener.
   * @VisibleForTesting
   */
  final Object getAttribute(final Attribute attribute) {
    Validate.notNull(attribute);
    return servletContext.getAttribute(getAttributeName(attribute));
  }
  
  /**
   * @return the {@link WroManagerFactory} stored in servletContext.
   */
  public WroManagerFactory getManagerFactory() {
    return (WroManagerFactory) getAttribute(Attribute.MANAGER_FACTORY);
  }
  
  /**
   * @return the {@link WroConfiguration} stored in servletContext.
   */
  public WroConfiguration getWroConfiguration() {
    return (WroConfiguration) getAttribute(Attribute.CONFIGURATION);
  }
  
  /**
   * Sets the attribute into the servlet context. The name of the attribute will be computed for you.
   */
  final void setAttribute(final Attribute attribute, final Object object) {
    Validate.notNull(attribute);
    LOG.debug("setting attribute: {} with value: {}", attribute, object);
    Validate.isTrue(attribute.isValid(object), object + " is not of valid subType for attribute: " + attribute);
    servletContext.setAttribute(getAttributeName(attribute), object);
  }

  /**
   * Set the configuration object as a servletContext attribute. 
   */
  public void setWroConfiguration(final WroConfiguration config) {
    setAttribute(Attribute.CONFIGURATION, config);
  }
  

  /**
   * Set the configuration object as a servletContext attribute. 
   */
  public void setManagerFactory(final WroManagerFactory managerFactory) {
    setAttribute(Attribute.MANAGER_FACTORY, managerFactory);
  }
  
  
  /**
   * Remove all attributes from {@link ServletContext}.
   */
  public void clear() {
    LOG.debug("destroying servletContext: {}", this.name);
    for (Attribute attribute : Attribute.values()) {
      servletContext.removeAttribute(getAttributeName(attribute));
    }
  }
  
  /**
   * @VisibleForTesting
   * @return the name of the listener used by this helper.
   */
  final String getName() {
    return name;
  }
}
