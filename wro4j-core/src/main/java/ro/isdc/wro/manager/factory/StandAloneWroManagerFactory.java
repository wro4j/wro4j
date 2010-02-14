/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactoryImpl;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.JawrCssMinifierProcessor;

/**
 * This factory will create a WroManager which is able to run itself outside of
 * a webContainer.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class StandAloneWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onBeforeCreate() {
    Context.set(new Context.StandAloneContext());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroModelFactory newModelFactory() {
    return new XmlModelFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected WroManager newManager() {
    return new WroManager() {
      /**
       * Just return false, without checking the request headers.
       */
      @Override
      protected boolean isGzipSupported() {
        return false;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupsProcessor = new GroupsProcessorImpl();
    groupsProcessor.setUriLocatorFactory(newUriLocatorFactory());
    groupsProcessor.addPostProcessor(new CssVariablesProcessor());
    groupsProcessor.addPostProcessor(new JSMinProcessor());
    groupsProcessor.addPostProcessor(new JawrCssMinifierProcessor());
    return groupsProcessor;
  }

  /**
   * {@inheritDoc}
   */
  private UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    // The order is important.
    factory.addUriLocator(new ServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
    return factory;
  }
}
