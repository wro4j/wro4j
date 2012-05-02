package ro.isdc.wro.model.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;

public class InjectorAwareWroModelFactoryDecorator
    extends WroModelFactoryDecorator {
  public InjectorAwareWroModelFactoryDecorator(final WroModelFactory modelFactory, final Injector injector) {
    super(modelFactory);
    Validate.notNull(injector);
    injector.inject(modelFactory);
  }
}
