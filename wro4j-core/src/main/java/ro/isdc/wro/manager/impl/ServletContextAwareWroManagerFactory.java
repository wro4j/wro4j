/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ro.isdc.wro.cache.impl.MapCacheStrategy;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.model.impl.ServletContextAwareXmlModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.processor.impl.ContentStripperResourceProcessor;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;
import ro.isdc.wro.processor.impl.UriProcessorImpl;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.resource.impl.ClasspathUriLocator;
import ro.isdc.wro.resource.impl.ServletContextUriLocator;
import ro.isdc.wro.resource.impl.UriLocatorFactoryImpl;
import ro.isdc.wro.resource.impl.UrlUriLocator;

/**
 * A WroManagerFactory implementation aware of running inside a web application
 * and capable to access a ServletContext reference.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class ServletContextAwareWroManagerFactory implements WroManagerFactory {
  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory
      .getLog(ServletContextAwareWroManagerFactory.class);

  /**
   * Manager instance. Using volatile keyword fix the problem with
   * double-checked locking in JDK 1.5.
   */
  private volatile WroManager manager;

  /**
   * Creates default singleton instance of manager, by initializing manager
   * dependencies with default values (processors). {@inheritDoc}
   */
  public final WroManager getInstance() {
    // use double-check locking
    if (this.manager == null) {
      synchronized (this) {
        if (this.manager == null) {
          this.manager = newManager();
        }
      }
    }
    return this.manager;
  }

  /**
   * @return {@link WroManager}
   */
  private WroManager newManager() {
    log.debug("<newManager>");
    final WroManager manager = new WroManager();
    manager.setUriProcessor(new UriProcessorImpl());
    manager.setModelFactory(new ServletContextAwareXmlModelFactory());
    manager.setGroupsProcessor(newGroupsProcessor());
    manager.setUriLocatorFactory(newUriLocatorFactory());
    manager.setCacheStrategy(new MapCacheStrategy<String, String>());
    log.debug("</newManager>");
    return manager;
  }

  /**
   * @return {@link GroupsProcessor} configured processor.
   */
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessorImpl groupProcessor = new GroupsProcessorImpl();
    final List<ResourcePreProcessor> cssPreProcessors = new ArrayList<ResourcePreProcessor>();
    cssPreProcessors.add(new CssUrlRewritingProcessor());
    groupProcessor.setCssPreProcessors(cssPreProcessors);
    final List<ResourcePostProcessor> postProcessors = new ArrayList<ResourcePostProcessor>();
    postProcessors.add(new ContentStripperResourceProcessor());
    groupProcessor.setAnyResourcePostProcessors(postProcessors);
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
