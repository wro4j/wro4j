/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.factory;

import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;


/**
 * A factory which contains most used processors.
 *
 * @author Alex Objelean
 * @created 2 apr 2011
 * @since 1.4.0
 */
public final class DefaultProcessorsFactory extends SimpleProcessorsFactory {
  public DefaultProcessorsFactory() {
    addPreProcessor(new CssImportPreProcessor());
    addPreProcessor(new CssUrlRewritingProcessor());
    addPreProcessor(new SemicolonAppenderPreProcessor());
    addPreProcessor(new JSMinProcessor());
    addPreProcessor(new JawrCssMinifierProcessor());

    addPostProcessor(new CssVariablesProcessor());
  }
}
