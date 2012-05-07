package ro.isdc.wro.http;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * Encapsulates the details of storing/retrieving attributes from {@link ServletContext} for a given named listener. Use
 * this class to retrieve wro4j related attributes, instead of hard-coding the name of the attribute to retrieve.
 * 
 * @author Alex Objelean
 * @created 7 May 2012
 * @since 1.4.6
 */
public class ServletContextAttributeHelper {
  private static final Logger LOG = LoggerFactory.getLogger(WroServletContextListener.class);
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
   */
  public static enum Attribute {
    CONFIGURATION(WroConfiguration.class), WRO_MANAGER_FACTORY(WroManagerFactory.class);
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
    this(servletContext, WroServletContextListener.DEFAULT_LISTENER_NAME);
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
    Validate.notNull(name);
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
  String getAttributeName(final Attribute attribute) {
    Validate.notNull(attribute);
    return WroServletContextListener.class.getName() + "-" + attribute.name() + "-" + this.name;
  }
  
  /**
   * @return the value of the attribute stored in {@link ServletContext} for this listener.
   */
  public Object getAttribute(final Attribute attribute) {
    Validate.notNull(attribute);
    return servletContext.getAttribute(getAttributeName(attribute));
  }
  
  /**
   * Sets the attribute into the servlet context. The name of the attribute will be computed for you.
   */
  public void setAttribute(final Attribute attribute, final Object object) {
    Validate.notNull(attribute);
    LOG.debug("setting attribute: {} with value: {}", attribute, object);
    Validate.isTrue(attribute.isValid(object), object + " is not of valid subType for attribute: " + attribute);
    servletContext.setAttribute(getAttributeName(attribute), object);
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
}
