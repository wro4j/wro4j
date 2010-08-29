/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.factory.UriLocatorFactoryImpl;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;

/**
 * An implementation with no processors set.
 *
 * @author Alex Objelean
 * @created Created on May 4, 2010
 */
public final class NoProcessorsWroManagerFactory extends BaseWroManagerFactory {
  @Override
  protected void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();
    groupsProcessor.setUriLocatorFactory(factory);
    factory.addUriLocator(new ServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
  }
}
