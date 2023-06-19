package ro.isdc.wro.model.factory;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.DestroyableLazyInitializer;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.Transformer;


/**
 * <p>Decorates the model factory with callback registry calls and other useful factories. Another responsibility of this
 * decorator is make model creation thread safe.</p>
 *
 * <p>This class doesn't extend {@link AbstractDecorator} because we have to enhance the decorated object with new
 * decorators.</p>
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public final class DefaultWroModelFactoryDecorator
    extends AbstractDecorator<WroModelFactory>
    implements WroModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultWroModelFactoryDecorator.class);
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private ReadOnlyContext context;
  @Inject
  private Injector injector;
  /**
   * Responsible for model caching
   */
  private final DestroyableLazyInitializer<WroModel> modelInitializer = new DestroyableLazyInitializer<WroModel>() {
    @Override
    protected WroModel initialize() {
      callbackRegistry.onBeforeModelCreated();
      final StopWatch watch = new StopWatch("Create Model");
      watch.start("createModel");
      WroModel model = null;
      try {
        final WroModelFactory modelFactory = decorate(getDecoratedObject());
        injector.inject(modelFactory);
        model = modelFactory.create();
        return model;
      } finally {
        authorizeModelResources(model);
        callbackRegistry.onAfterModelCreated();
        watch.stop();
        LOG.debug(watch.prettyPrint());
      }
    }

    /**
     * Decorate with several useful aspects, like: fallback, caching & model transformer ability.
     */
    private WroModelFactory decorate(final WroModelFactory decorated) {
      return new ModelTransformerFactory(new FallbackAwareWroModelFactory(decorated)).setTransformers(modelTransformers);
    }

    /**
     * Authorizes all resources of the model to be accessed as proxy resources (only in dev mode).
     *
     * @param model
     *          {@link WroModel} created by decorated factory.
     */
    private void authorizeModelResources(final WroModel model) {
      if (model != null && context.getConfig().isDebug()) {
        if (authorizationManager instanceof MutableResourceAuthorizationManager) {
          for (final Resource resource : new WroModelInspector(model).getAllUniqueResources()) {
            ((MutableResourceAuthorizationManager) authorizationManager).add(resource.getUri());
          }
        }
      }
    }
  };

  private final List<Transformer<WroModel>> modelTransformers;

  /**
   * Factory method which takes care of redundant decoration.
   */
  public static WroModelFactory decorate(final WroModelFactory decorated,
      final List<Transformer<WroModel>> modelTransformers) {
    return decorated instanceof DefaultWroModelFactoryDecorator ? decorated : new DefaultWroModelFactoryDecorator(
        decorated, modelTransformers);
  }

  private DefaultWroModelFactoryDecorator(final WroModelFactory decorated,
      final List<Transformer<WroModel>> modelTransformers) {
    super(decorated);
    Validate.notNull(modelTransformers);

    this.modelTransformers = modelTransformers;
  }

  public WroModel create() {
    return modelInitializer.get();
  }

  public void destroy() {
    LOG.debug("Destroy model");
    modelInitializer.destroy();
    getDecoratedObject().destroy();
    if (authorizationManager instanceof MutableResourceAuthorizationManager) {
      ((MutableResourceAuthorizationManager) authorizationManager).clear();
    }
  }
}
