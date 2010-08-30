/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory using experimental features, like {@link CssDataUriPreProcessor} which is not fully supported by all
 * browsers.
 *
 * @author Alex Objelean
 * @created May 9, 2010
 */
public class ExperimentalWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
    groupsProcessor.setUriLocatorFactory(newUriLocatorFactory());

    groupsProcessor.addPreProcessor(new CssDataUriPreProcessor());
    groupsProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupsProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupsProcessor.addPreProcessor(new CssImportPreProcessor());
    groupsProcessor.addPreProcessor(new SemicolonAppenderPreProcessor());

    groupsProcessor.addPostProcessor(new CssVariablesProcessor());
    groupsProcessor.addPostProcessor(new JSMinProcessor());
    groupsProcessor.addPostProcessor(new JawrCssMinifierProcessor());
  }


  /**
   * Creates a new {@link UriLocatorFactory} implementation.
   *
   * @return {@link IUriLocatorFactory} object.
   */
  private UriLocatorFactory newUriLocatorFactory() {
    final UriLocatorFactory factory = new UriLocatorFactory();
    factory.addUriLocator(new ServletContextUriLocator());
    factory.addUriLocator(new ClasspathUriLocator());
    factory.addUriLocator(new UrlUriLocator());
    return factory;
  }

}
