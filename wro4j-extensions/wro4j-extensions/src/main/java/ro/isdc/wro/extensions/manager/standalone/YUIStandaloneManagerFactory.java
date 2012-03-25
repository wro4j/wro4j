/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager.standalone;

import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory using YUI js & css compressor for processing resources.
 *
 * @author Alex Objelean
 */
public class YUIStandaloneManagerFactory extends ExtensionsStandaloneManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(YUIJsCompressorProcessor.doMungeCompressor());
    factory.addPreProcessor(new YUICssCompressorProcessor());

    factory.addPostProcessor(new CssVariablesProcessor());
    return factory;
  }
}
