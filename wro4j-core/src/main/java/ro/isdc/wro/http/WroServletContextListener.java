package ro.isdc.wro.http;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.factory.PropertyWroConfigurationFactory;
import ro.isdc.wro.config.jmx.WroConfiguration;

/**
 * A listener which loads wroConfiguration and stores it in servletContext.
 *
 * @author Alex Objelean
 * @created 6 May 2012
 * @since 1.4.6
 */
public class WroServletContextListener
    implements ServletContextListener {
  private static final Logger LOG = LoggerFactory.getLogger(WroServletContextListener.class);

  /**
   * An application can have multiple context listeners. Each listener can have its own name. If no name is provided, a
   * default one is used.
   */
  private static final String CONTEXT_NAME = "name";
  private static final String DEFAULT_LISTENER_NAME = "default";
  private WroConfiguration configuration;
  private ServletContext servletContext;
  
  public static enum Attribute {
    CONFIGURATION, WRO_MANAGER_FACTORY
  }

  /**
   * {@inheritDoc}
   */
  public void contextInitialized(final ServletContextEvent event) {
    this.servletContext = event.getServletContext();
    initListener(event.getServletContext());
  }
  

  private void initListener(final ServletContext servletContext) {
    final String listenerName = getListenerName();
    LOG.debug("initializing listener with name: {}", listenerName);
    if (getAttributeValue(Attribute.CONFIGURATION) != null) {
      final String message = "Cannot initialize context because there is already a listener present - withName: "
          + listenerName + ". Check whether you have multiple listener* definitions in your web.xml!";
      LOG.error(message);
      throw new IllegalStateException(message);
    }
    this.configuration = createConfiguration();
    setAttributeValue(Attribute.CONFIGURATION, this.configuration);
  }
  
  /**
   * @return the value of the attribute stored in {@link ServletContext} for this listener.
   */
  private Object getAttributeValue(final Attribute attribute) {
    return servletContext.getAttribute(getAttributeName(attribute));
  }
  
  /**
   * Sets the attribute into the servlet context. The name of the attribute will be computed for you.
   */
  private void setAttributeValue(final Attribute attribute, final Object object) {
    servletContext.setAttribute(getAttributeName(attribute), object);
  }

  /**
   * @param attribute
   *          type of attribute.
   * @return the name of the attribute used to store in servlet context.
   */
  private String getAttributeName(final Attribute attribute) {
    return WroServletContextListener.class.getName() + "-" + attribute.name() + "-" + getListenerName();
  }


  /**
   * Retrieve the name listener name. Check the init param. If no value is provided a default value is used.
   */
  private String getListenerName() {
    final String initParamName = servletContext.getInitParameter(CONTEXT_NAME); 
    return initParamName == null ? DEFAULT_LISTENER_NAME : initParamName;
  }


  /**
   * Create the ContextLoader to use. Can be overridden in subclasses.
   * @return the new ContextLoader
   */
  protected WroConfiguration createConfiguration() {
    return new PropertyWroConfigurationFactory().create();
  }
  
  /**
   * {@inheritDoc}
   */
  public void contextDestroyed(final ServletContextEvent servletContextEvent) {
  }

  /**
   * @VisibleForTesting
   * @return the {@link WroConfiguration} object built by this listener.
   */
  WroConfiguration getConfiguration() {
    return configuration;
  }
}
