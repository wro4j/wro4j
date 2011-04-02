/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.manager.factory;

import ro.isdc.wro.model.resource.processor.factory.DefaultProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;


/**
 * A factory using experimental features, like {@link CssDataUriPreProcessor} which is not fully supported by all
 * browsers.
 *
 * @author Alex Objelean
 * @created May 9, 2010
 */
public class ExperimentalWroManagerFactory extends BaseWroManagerFactory {
  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new DefaultProcessorsFactory();
    factory.addPreProcessor(new CssDataUriPreProcessor());
    return factory;
  }
}
