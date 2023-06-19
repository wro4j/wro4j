/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.manager.standalone;

import com.google.javascript.jscomp.CompilationLevel;

import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory using google closure compressor with advanced compilation level for processing resources.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public class GoogleAdvancedStandaloneManagerFactory extends ExtensionsStandaloneManagerFactory {
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
    factory.addPreProcessor(new JawrCssMinifierProcessor());

    factory.addPostProcessor(new CssVariablesProcessor());
    return factory;
  }
}
