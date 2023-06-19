/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.factory;

import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;



/**
 * Default {@link ProcessorsFactory} which provides most commons processors.
 *
 * @author Alex Objelean
 * @since 1.3.7
 */
public final class DefaultProcessorsFactory
  extends SimpleProcessorsFactory {
  public DefaultProcessorsFactory() {
    addPreProcessor(new CssUrlRewritingProcessor());
    addPreProcessor(new CssImportPreProcessor());
    addPreProcessor(new SemicolonAppenderPreProcessor());
    addPreProcessor(new JSMinProcessor());
    addPreProcessor(new JawrCssMinifierProcessor());
    addPostProcessor(new CssVariablesProcessor());
  }
}
