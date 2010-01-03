/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import ro.isdc.wro.extensions.processor.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.impl.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.processor.impl.CssVariablesProcessor;
import ro.isdc.wro.processor.impl.GroupsProcessorImpl;

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
    groupProcessor.addCssPreProcessor(new CssUrlRewritingProcessor());
    groupProcessor.addCssPreProcessor(new CssVariablesProcessor());
    groupProcessor.addCssPostProcessor(new YUICssCompressorProcessor());
    groupProcessor.addJsPostProcessor(new YUIJsCompressorProcessor());
    return groupProcessor;
  }
}
