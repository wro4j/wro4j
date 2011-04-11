/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.model.resource.processor.factory.DefaultProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * A WroManagerFactory implementation aware of running inside a web application and capable to access a ServletContext
 * reference. Use several predefined processors (like jsMin, cssMin, etc) & 3 most used uriLocators (like
 * servletContext, classpath & urlUri)
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 * @deprecated TODO remove
 */
@Deprecated
public class ServletContextAwareWroManagerFactory
    extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    return new DefaultProcessorsFactory();
  }
}
