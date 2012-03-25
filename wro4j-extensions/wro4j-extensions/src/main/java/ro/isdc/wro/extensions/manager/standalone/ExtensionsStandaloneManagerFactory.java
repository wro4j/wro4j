/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager.standalone;

import ro.isdc.wro.extensions.model.factory.SmartWroModelFactory;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.factory.WroModelFactory;


/**
 * This factory uses the {@link SmartWroModelFactory} and it is recommended to be used as the default one when possible.
 *
 * @author Alex Objelean
 * @created 9 Aug 2011
 * @since 1.4.0
 */
public class ExtensionsStandaloneManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
  private StandaloneContext context;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(final StandaloneContext standaloneContext) {
    super.initialize(standaloneContext);
    this.context = standaloneContext;
  }


  @Override
  protected WroModelFactory newModelFactory() {
    return SmartWroModelFactory.createFromStandaloneContext(context);
  }
}
