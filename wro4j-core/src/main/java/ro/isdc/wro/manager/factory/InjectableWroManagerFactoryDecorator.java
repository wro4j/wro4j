package ro.isdc.wro.manager.factory;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;


/**
 * Responsible for injecting {@link WroManager} each time it is created by decorated factory.
 *
 * @author Alex Objelean
 * @created 23 Jun 2012
 * @since 1.4.7
 * @deprecated Not required anymore. The {@link WroManager} objects are injected in {@link WroManager.Builder} during
 *             initialization. This class with be removed in 1.7.0.
 */
@Deprecated
public class InjectableWroManagerFactoryDecorator
    extends WroManagerFactoryDecorator {
  private Injector injector;

  public InjectableWroManagerFactoryDecorator(final WroManagerFactory decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroManager create() {
    return getDecoratedObject().create();
  }

  /**
   * @return {@link Injector} used to inject the created manager.
   */
  @Deprecated
  public Injector getInjector() {
    if (injector == null) {
      injector = InjectorBuilder.create(getDecoratedObject()).build();
    }
    return injector;
  }
}
