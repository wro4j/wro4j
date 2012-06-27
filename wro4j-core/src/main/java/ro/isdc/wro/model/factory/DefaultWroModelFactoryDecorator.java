package ro.isdc.wro.model.factory;

import java.util.List;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.ObjectDecorator;
import ro.isdc.wro.util.Transformer;


/**
 * Decorates the model factory with callback registry calls & other useful factories. Another responsibility of this
 * decorator is make model creation thread safe.
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
  private final WroModelFactory decorated;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private WroConfiguration config;

  private final List<Transformer<WroModel>> modelTransformers;
  
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
    return new InMemoryCacheableWroModelFactory(
        new ModelTransformerFactory(new FallbackAwareWroModelFactory(decorated)).setTransformers(modelTransformers));
  }
  
  /**
   * {@inheritDoc}
   */
  public WroModel create() {
    callbackRegistry.onBeforeModelCreated();
    WroModel model = null;
    try {
      model = getDecoratedObject().create();
      return model;
    } finally {
      authorizeModelResources(model);
      callbackRegistry.onAfterModelCreated();
    }
  }
  
  /**
   * Authorizes all resources of the model to be accessed as proxy resources (only in dev mode).
   * 
   * @param model
   *          {@link WroModel} created by decorated factory.
   */
  private void authorizeModelResources(final WroModel model) {
    if (model != null && config.isDebug()) {
      for (Resource resource : model.getAllResources()) {
        authorizationManager.add(resource.getUri());
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void destroy() {
    getDecoratedObject().destroy();
    //reset authorization manager (clear any stored uri's).
    authorizationManager.clear();
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
