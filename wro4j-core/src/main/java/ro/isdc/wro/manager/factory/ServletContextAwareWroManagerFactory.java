/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.resource.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A WroManagerFactory implementation aware of running inside a web application and capable to access a ServletContext
 * reference. Use several predefined processors (like jsMin, cssMin, etc) & 3 most used uriLocators (like
 * servletContext, classpath & urlUri)
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class ServletContextAwareWroManagerFactory
    extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected final WroManager newWroManager() {
    return new WroManager() {
      @Override
      protected ProcessorsFactory newProcessorsFactory() {
        return ServletContextAwareWroManagerFactory.this.newProcessorsFactory();
      }
      @Override
      protected UriLocatorFactory newUriLocatorFactory() {
        final UriLocatorFactory factory = new SimpleUriLocatorFactory().addUriLocator(
            new ServletContextUriLocator()).addUriLocator(new ClasspathUriLocator()).addUriLocator(new UrlUriLocator());
        return factory;
      }
    };
  }

  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new BomStripperPreProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());

    factory.addPostProcessor(new CssVariablesProcessor());
    factory.addPostProcessor(new JSMinProcessor());
    factory.addPostProcessor(new JawrCssMinifierProcessor());
    return factory;
  }
}
