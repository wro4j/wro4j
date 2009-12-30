/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.manager.impl;

import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.JSMinProcessor;
import ro.isdc.wro.processor.impl.JawrCssMinifierProcessor;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;

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
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupProcessor = new GroupsProcessorImpl();
    groupProcessor.addCssPostProcessor(new CssVariablesProcessor());
    groupProcessor.addJsPostProcessor(new JSMinProcessor());
    groupProcessor.addCssPostProcessor(new JawrCssMinifierProcessor());
    return groupProcessor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactory factory = new UriLocatorFactoryImpl();
    // The order is important.
    factory.addUriLocator(new ServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
    return factory;
  }
}
