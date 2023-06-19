/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory.standalone;

import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;

/**
 * This factory will create a WroManager which is able to run itself outside of
 * a webContainer.
 *
 * @author Alex Objelean
 */
public class StandaloneWroManagerFactory extends BaseWroManagerFactory {

  @Override
  protected WroModelFactory newModelFactory() {
    return new XmlModelFactory();
  }

  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    return new SimpleUriLocatorFactory().addLocator(newServletContextUriLocator()).addLocators(
        new DefaultUriLocatorFactory().getUriLocators());
  }

  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    return new SimpleProcessorsFactory();
  }


  /**
   * @return {@link ServletContextUriLocator} or a derivate locator which will be responsible for locating resources
   *         starting with '/' character. Ex: /static/resource.js
   */
  protected ServletContextUriLocator newServletContextUriLocator() {
    return new ServletContextUriLocator();
  }
}
