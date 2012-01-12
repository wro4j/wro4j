package ro.isdc.wro.examples.manager;

import ro.isdc.wro.extensions.manager.ExtensionsConfigurableWroManagerFactory;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.PerformanceLoggerCallback;

/**
 * @author Alex Objelean
 */
public class CustomExtensionsWroManagerFactory
    extends ExtensionsConfigurableWroManagerFactory {
  @Override
  protected void onAfterInitializeManager(final WroManager manager) {
    manager.registerCallback(new PerformanceLoggerCallback());
  }
}
