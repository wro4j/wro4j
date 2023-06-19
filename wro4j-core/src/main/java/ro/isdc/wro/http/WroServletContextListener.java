package ro.isdc.wro.http;

import static org.apache.commons.lang3.Validate.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import ro.isdc.wro.config.factory.ServletContextPropertyWroConfigurationFactory;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;
import ro.isdc.wro.manager.factory.DefaultWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;


/**
 * A listener which loads wroConfiguration and stores it in servletContext. If you want to have multiple listeners,
 * extend this class and override the {@link WroServletContextListener#getListenerName()} to provide a unique non-empty
 * name (defaulted to "default").
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public class WroServletContextListener
    implements ServletContextListener {
  private static final Logger LOG = LoggerFactory.getLogger(WroServletContextListener.class);
  private WroConfiguration configuration;
  private WroManagerFactory managerFactory;
  private ServletContext servletContext;
  private ServletContextAttributeHelper attributeHelper;

  /**
   * {@inheritDoc}
   */
  public final void contextInitialized(final ServletContextEvent event) {
    this.servletContext = event.getServletContext();
    attributeHelper = new ServletContextAttributeHelper(this.servletContext, getListenerName());
    initListener(event.getServletContext());
  }

  private void initListener(final ServletContext servletContext) {
    if (attributeHelper.getWroConfiguration() != null || attributeHelper.getManagerFactory() != null) {
      final String message = "Cannot initialize context because there is already a listener present - withName: "
          + getListenerName()
          + ". Check whether you have multiple listener* (of type WroServletContextListener) definitions in your web.xml!";
      LOG.error(message);
      throw new IllegalStateException(message);
    }
    // create configuration first because managerFactory require it during creation.
    this.configuration = createConfiguration();
    this.managerFactory = createManagerFactory();
    LOG.debug("Loaded managerFactory: {}", this.managerFactory.getClass());

    attributeHelper.setWroConfiguration(this.configuration);
    attributeHelper.setManagerFactory(this.managerFactory);
  }

  /**
   * @return a not null {@link WroConfiguration} object.
   */
  private WroConfiguration createConfiguration() {
    return this.configuration != null ? this.configuration : newConfiguration();
  }

  /**
   * Create the ContextLoader to use. Can be overridden in subclasses.
   *
   * @return the new ContextLoader
   */
  protected WroConfiguration newConfiguration() {
    return new ServletContextPropertyWroConfigurationFactory(servletContext).create();
  }

  /**
   * @return decorated instance of {@link WroManagerFactory}.
   */
  private WroManagerFactory createManagerFactory() {
    return this.managerFactory != null ? managerFactory : newManagerFactory();
  }

  /**
   * @return default implementation of {@link WroManagerFactory}.
   */
  protected WroManagerFactory newManagerFactory() {
    return DefaultWroManagerFactory.create(configuration);
  }

  /**
   * {@inheritDoc}
   */
  public final void contextDestroyed(final ServletContextEvent servletContextEvent) {
    attributeHelper.clear();
  }

  /**
   * Set the manager factory to be initialized by this listener.
   *
   * @param managerFactory
   *          a not null manager instance.
   */
  public final void setManagerFactory(final WroManagerFactory managerFactory) {
    notNull(managerFactory);
    this.managerFactory = managerFactory;
  }

  /**
   * @return the name of the listener. Override it to provide a different name if you need to configure multiple
   *         listener.
   */
  protected String getListenerName() {
    return ServletContextAttributeHelper.DEFAULT_NAME;
  }

  /**
   * Set the configuration to be initialized by this listener.
   *
   * @param configuration
   *          a not null configuration instance.
   */
  public final void setConfiguration(final WroConfiguration configuration) {
    notNull(configuration);
    this.configuration = configuration;
  }

  /**
   * @VisibleForTesting
   * @return the {@link WroConfiguration} object built by this listener.
   */
  final WroConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * @VisibleForTesting
   * @return the {@link WroManagerFactory} object built by this listener.
   */
  final WroManagerFactory getManagerFactory() {
    return managerFactory;
  }
}
