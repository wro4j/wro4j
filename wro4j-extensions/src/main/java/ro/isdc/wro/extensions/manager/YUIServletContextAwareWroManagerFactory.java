/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager;

import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;
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
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new BomStripperPreProcessor());
    factory.addPostProcessor(new CssVariablesProcessor());
    factory.addPostProcessor(new YUICssCompressorProcessor());
    factory.addPostProcessor(new YUIJsCompressorProcessor());
    return factory;
  }

}
