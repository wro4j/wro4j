/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.factory;

import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;



/**
 * Default {@link ProcessorsFactory} which provides most commons processors.
 *
 * @author Alex Objelean
 * @created 15 May 2011
 * @since 1.3.7
 */
public final class DefaultProcesorsFactory
  extends SimpleProcessorsFactory {
  public DefaultProcesorsFactory() {
//    addPreProcessor(new CssUrlRewritingProcessor());
//    addPreProcessor(new CssImportPreProcessor());
//    addPreProcessor(new BomStripperPreProcessor());
//    addPreProcessor(new SemicolonAppenderPreProcessor());
    addPreProcessor(new JSMinProcessor());
//    addPreProcessor(new JawrCssMinifierProcessor());
//    addPostProcessor(new CssVariablesProcessor());
  }
}
