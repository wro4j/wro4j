/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.maven;

import ro.isdc.wro.extensions.processor.yui.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.yui.YUIJsCompressorProcessor;
import ro.isdc.wro.maven.plugin.DefaultMavenContextAwareManagerFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory using YUI js & css compressor for processing resources.
 *
 * @author Alex Objelean
 */
public class YUIMavenManagerFactory extends DefaultMavenContextAwareManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureProcessors(final GroupsProcessor groupsProcessor) {
    groupsProcessor.addPreProcessor(new BomStripperPreProcessor());
    groupsProcessor.addPreProcessor(new CssImportPreProcessor());
    groupsProcessor.addPreProcessor(new CssUrlRewritingProcessor());
    groupsProcessor.addPreProcessor(new SemicolonAppenderPreProcessor());
    groupsProcessor.addPostProcessor(new CssVariablesProcessor());
    groupsProcessor.addPostProcessor(new YUIJsCompressorProcessor());
    groupsProcessor.addPostProcessor(new YUICssCompressorProcessor());
  }
}
