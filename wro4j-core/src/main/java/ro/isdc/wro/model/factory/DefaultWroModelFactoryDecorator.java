package ro.isdc.wro.model.factory;

import java.util.List;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.ObjectDecorator;
import ro.isdc.wro.util.Transformer;


/**
 * Decorates the model factory with callback registry calls & other useful factories.
 * <p/>
 * This class doesn't extend {@link AbstractDecorator} because we have to enhance the decorated object with new
 * decorators.
 * 
 * @author Alex Objelean
 * @created 13 Mar 2011
 * @since 1.4.6
 */
public class DefaultWroModelFactoryDecorator
    implements WroModelFactory, ObjectDecorator<WroModelFactory> {
  private WroModelFactory decorated;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  private List<Transformer<WroModel>> modelTransformers;
  
  public DefaultWroModelFactoryDecorator(final WroModelFactory decorated,
      final List<Transformer<WroModel>> modelTransformers) {
    this.modelTransformers = modelTransformers;
    this.decorated = enhance(decorated);
    Validate.notNull(modelTransformers);
  }
  
  /**
   * Decorate with several useful aspects, like: fallback, caching & model transformer ability.
   */
  private WroModelFactory enhance(final WroModelFactory decorated) {
    return new ModelTransformerFactory(
        new InMemoryCacheableWroModelFactory(new FallbackAwareWroModelFactory(decorated))).setTransformers(modelTransformers);
  }
  
  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    callbackRegistry.onBeforeModelCreated();
    try {
      return getDecoratedObject().create();
    } finally {
      callbackRegistry.onAfterModelCreated();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedObject().destroy();
  }
  
  /**
   * {@inheritDoc}
   */
  public WroModelFactory getDecoratedObject() {
    return this.decorated;
  }
  

  /**
   * {@inheritDoc}
   */
  public WroModelFactory getOriginalDecoratedObject() {
    return AbstractDecorator.getOriginalDecoratedObject(getDecoratedObject());
  }
}
