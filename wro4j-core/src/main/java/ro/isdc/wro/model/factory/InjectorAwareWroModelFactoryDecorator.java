package ro.isdc.wro.model.factory;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.Transformer;

public class InjectorAwareWroModelFactoryDecorator
    extends WroModelFactoryDecorator {
  private final Injector injector;

  public InjectorAwareWroModelFactoryDecorator(final WroModelFactory modelFactory, final Injector injector) {
    super(modelFactory);
    Validate.notNull(injector);
    this.injector = injector;
    inject(modelFactory);
  }

  /**
   * Handles injection in decorated factories (when decorator is detected) and for model transformers. 
   */
  private void inject(final WroModelFactory modelFactory) {
    injector.inject(modelFactory);
    if (modelFactory instanceof WroModelFactoryDecorator) {
      inject(((WroModelFactoryDecorator)modelFactory).getDecoratedObject());
    }
    if (modelFactory instanceof ModelTransformersAware) {
      for (Transformer<WroModel> transformer : ((ModelTransformersAware) modelFactory).getModelTransformers()) {
        injector.inject(transformer);
      }
    }
  }
}
