/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.ServletContextResourceLocator;

/**
 * ServletContext aware Model factory implementation. This factory will run
 * properly only when is used inside a web application. The configuration xml
 * file will be read from the following location: <code>/WEB-INF/wro.xml</code>
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public final class ServletContextAwareXmlModelFactory extends XmlModelFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceLocator getResourceLocator() {
    return new ServletContextResourceLocator(Context.get().getServletContext(), "/WEB-INF/" + XML_CONFIG_FILE);
  }
}
