package ro.isdc.wro.examples.manager;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.PerformanceLoggerCallback;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.util.ObjectFactory;


/**
 * @author Alex Objelean
 */
public class CustomExtensionsWroManagerFactory
    extends ConfigurableWroManagerFactory {
  @Override
  protected void onAfterInitializeManager(final WroManager manager) {
    manager.registerCallback(new ObjectFactory<LifecycleCallback>() {
      @Override
      public LifecycleCallback create() {
        return new PerformanceLoggerCallback();
      }
    });
  }
}
