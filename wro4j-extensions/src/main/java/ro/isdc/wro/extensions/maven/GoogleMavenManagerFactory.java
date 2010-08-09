/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.maven;

import ro.isdc.wro.extensions.processor.google.GoogleClosureCompressorProcessor;
import ro.isdc.wro.manager.factory.maven.DefaultMavenContextAwareManagerFactory;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory using google closure compressor for processing resources.
 *
 * @author Alex Objelean
 */
public class GoogleMavenManagerFactory extends DefaultMavenContextAwareManagerFactory {
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
    groupsProcessor.addPostProcessor(new GoogleClosureCompressorProcessor());
    groupsProcessor.addPostProcessor(new JawrCssMinifierProcessor());
  }
}
