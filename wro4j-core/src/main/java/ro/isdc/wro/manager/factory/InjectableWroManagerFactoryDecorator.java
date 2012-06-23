package ro.isdc.wro.manager.factory;

import org.apache.commons.lang3.Validate;

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
    this(decorated, InjectorBuilder.create(decorated).build());
  }
  
  public InjectableWroManagerFactoryDecorator(final WroManagerFactory decorated, final Injector injector) {
    super(decorated);
    Validate.notNull(injector);
    this.injector = injector;
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
  protected Injector getInjector() {
    return injector;
  }
}
