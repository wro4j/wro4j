/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import ro.isdc.wro.extensions.processor.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.GroupsProcessorImpl;
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
  protected GroupsProcessor newGroupsProcessor() {
    final GroupsProcessor groupProcessor = new GroupsProcessorImpl();
    groupProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupProcessor.addPreProcessor(new CssImportPreProcessor());
    groupProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupProcessor.addPostProcessor(new CssVariablesProcessor());
    groupProcessor.addPostProcessor(new YUICssCompressorProcessor());
    groupProcessor.addPostProcessor(new YUIJsCompressorProcessor());
    return groupProcessor;
  }
}
