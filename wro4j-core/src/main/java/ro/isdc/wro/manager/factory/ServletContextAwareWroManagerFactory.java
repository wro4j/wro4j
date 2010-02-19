/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactoryImpl;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.JawrCssMinifierProcessor;

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
    final CssImportPreProcessor cssImportProcessor = new CssImportPreProcessor();

    final GroupsProcessor groupProcessor = new GroupsProcessorImpl();
    groupProcessor.setUriLocatorFactory(newUriLocatorFactory());

    groupProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupProcessor.addPreProcessor(cssImportProcessor);

    groupProcessor.addPostProcessor(new CssVariablesProcessor());
    groupProcessor.addPostProcessor(new JSMinProcessor());
    groupProcessor.addPostProcessor(new JawrCssMinifierProcessor());

    return groupProcessor;
  }

  /**
   * Creates a new {@link UriLocatorFactory} implementation.
   */
  private UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    factory.addUriLocator(new ServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
    return factory;
  }
}
