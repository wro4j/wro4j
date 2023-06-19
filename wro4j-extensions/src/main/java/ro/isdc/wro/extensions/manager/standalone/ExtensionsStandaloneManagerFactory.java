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
 * @since 1.4.0
 */
public class ExtensionsStandaloneManagerFactory extends DefaultStandaloneContextAwareManagerFactory {
  @Override
  public void initialize(final StandaloneContext standaloneContext) {
    super.initialize(standaloneContext);
  }

  @Override
  protected WroModelFactory newModelFactory() {
    return SmartWroModelFactory.createFromStandaloneContext(getStandaloneContext());
  }
}
