/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.JSMinProcessor;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;

/**
 * A WroManagerFactory implementation aware of running inside a web application
 * and capable to access a ServletContext reference. Use several predefined processors (like jsMin, cssMin, etc) & 3 most used uriLocators (like servletContext, classpath & urlUri)
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class ServletContextAwareWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupProcessor = new GroupsProcessorImpl();
    groupProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupProcessor.addPostProcessor(new CssVariablesProcessor());
    groupProcessor.addPostProcessor(new JSMinProcessor());
    //groupProcessor.addPostProcessor(new JawrCssMinifierProcessor());
    return groupProcessor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    factory.addUriLocator(new ServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
    return factory;
  }
}
