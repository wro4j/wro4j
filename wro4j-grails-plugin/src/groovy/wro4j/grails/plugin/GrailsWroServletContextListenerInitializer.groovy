package wro4j.grails.plugin

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent

import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware
import ro.isdc.wro.config.factory.PropertyWroConfigurationFactory
import ro.isdc.wro.http.WroServletContextListener
import ro.isdc.wro.manager.factory.WroManagerFactory

public class GrailsWroServletContextListenerInitializer implements ServletContextAware, DisposableBean {
    private ServletContext servletContext;
    private WroServletContextListener wroServletContextListener;
    private ro.isdc.wro.manager.factory.WroManagerFactory wroManagerFactory;
    private Properties properties;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        PropertyWroConfigurationFactory propertyWroConfigurationFactory = new PropertyWroConfigurationFactory(properties);

        wroServletContextListener = new WroServletContextListener();
        wroServletContextListener.setConfiguration(propertyWroConfigurationFactory.create());
        wroServletContextListener.setManagerFactory(wroManagerFactory);
        wroServletContextListener.contextInitialized(new ServletContextEvent(servletContext));
    }

    @Override
    public void destroy() throws Exception {
        wroServletContextListener.contextDestroyed(new ServletContextEvent(servletContext));
    }

	@Required
    public void setWroManagerFactory(WroManagerFactory wroManagerFactory) {
        this.wroManagerFactory = wroManagerFactory;
    }

	@Required
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
