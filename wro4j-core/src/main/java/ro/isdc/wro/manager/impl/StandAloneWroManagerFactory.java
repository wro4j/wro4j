/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.cache.impl.MapCacheStrategy;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.model.impl.XmlModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.impl.ContentStripperResourceProcessor;
import ro.isdc.wro.processor.impl.CssVariablesPreprocessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.JSMinProcessor;
import ro.isdc.wro.processor.impl.UriProcessorImpl;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;

/**
 * This factory will create a WroManager which is able to run itself outside of
 * a webContainer.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class StandAloneWroManagerFactory implements WroManagerFactory {
  /**
   * Manager instance.
   */
  private WroManager manager;

  /**
   * Creates default instance of factory, by initializing manager dependencies
   * with default values (processors).
   */
  public final synchronized WroManager getInstance() {
    Context.set(new Context.StandAloneContext());
    if (this.manager == null) {
      this.manager = newManager();
    }
    return this.manager;
  }

  /**
   * @param settings
   *          {@link WroSettings} object.
   * @return {@link WroManager}
   */
  private WroManager newManager() {
    final WroManager manager = new WroManager();
    manager.setUriProcessor(new UriProcessorImpl());
    manager.setModelFactory(newModelFactory());
    manager.setGroupsProcessor(newGroupsProcessor());
    manager.setUriLocatorFactory(newUriLocatorFactory());
    manager.setCacheStrategy(new MapCacheStrategy<String, String>());
    return manager;
  }

  /**
   * @return an implementation of {@link WroManagerFactory}.
   */
  protected WroModelFactory newModelFactory() {
    return new XmlModelFactory();
  }

  /**
   * @return {@link GroupsProcessor} implementation.
   */
  private GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupProcessor = new GroupsProcessorImpl();
    groupProcessor.addCssPreProcessor(new CssVariablesPreprocessor());
    groupProcessor.addJsPostProcessor(new JSMinProcessor());
    groupProcessor.addAnyPostProcessor(new ContentStripperResourceProcessor());
    return groupProcessor;
  }

  /**
   * Factory method for {@link UriLocatorFactory}. Create a factory and
   * initialize the uriLocators to be used.
   *
   * @return UriLocatorFactory implementation.
   */
  protected UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactoryImpl factory = new UriLocatorFactoryImpl();

    final List<UriLocator> resourceLocators = new ArrayList<UriLocator>();
    // populate the list. The order is important.
    resourceLocators.add(new ServletContextUriLocator());
    resourceLocators.add(new ClasspathUriLocator());
    resourceLocators.add(new UrlUriLocator());

    factory.setUriLocators(resourceLocators);
    return factory;
  }
}
