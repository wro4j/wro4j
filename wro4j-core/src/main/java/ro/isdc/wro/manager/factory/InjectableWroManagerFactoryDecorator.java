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
 */
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
    final WroManager manager = getDecoratedObject().create();
    getInjector().inject(manager);
    return manager;
  }

  /**
   * @return {@link Injector} used to inject the created manager.
   */
  public Injector getInjector() {
    if (injector == null) {
      injector = InjectorBuilder.create(getDecoratedObject()).build();
    }
    return injector;
  }
}
