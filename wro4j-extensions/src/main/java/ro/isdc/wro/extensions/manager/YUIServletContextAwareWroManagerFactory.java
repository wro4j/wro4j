/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;

/**
 * A factory which use YUI specific GroupProcessors
 *
 * @author Alex Objelean
 */
public class YUIServletContextAwareWroManagerFactory extends ServletContextAwareWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureGroupsProcessor(final GroupsProcessor groupsProcessor) {
    groupsProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupsProcessor.addPreProcessor(new CssImportPreProcessor());
    groupsProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupsProcessor.addPreProcessor(new YUICssCompressorProcessor());
    groupsProcessor.addPreProcessor(new YUIJsCompressorProcessor());

    groupsProcessor.addPostProcessor(new CssVariablesProcessor());
  }
}
