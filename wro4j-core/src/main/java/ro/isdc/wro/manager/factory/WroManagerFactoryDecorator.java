package ro.isdc.wro.manager.factory;

import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.WroManager.Builder;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAware;
import ro.isdc.wro.util.AbstractDecorator;
import ro.isdc.wro.util.DestroyableLazyInitializer;


/**
 * <p>Simple decorator for {@link WroManagerFactory}.</p>
 *
 * <p>This class implements als the {@link StandaloneContextAware} in order to allow decoration of factories used in
 * standalone context (example: maven plugin).</p>
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class WroManagerFactoryDecorator
    extends AbstractDecorator<WroManagerFactory>
    implements WroManagerFactory, StandaloneContextAware {

  private final DestroyableLazyInitializer<WroManager> managerInitializer = new DestroyableLazyInitializer<WroManager>() {
    @Override
    protected WroManager initialize() {
      final WroManager.Builder builder = new WroManager.Builder(getDecoratedObject().create());
      onBeforeBuild(builder);
      return builder.build();
    }

    @Override
    public void destroy() {
      getDecoratedObject().destroy();
      super.destroy();
    }
  };

  public WroManagerFactoryDecorator(final WroManagerFactory managerFactory) {
    super(managerFactory);
  }

  public WroManager create() {
    return managerInitializer.get();
  }

  /**
   * Allows client code to change the builder before the {@link WroManager} is created.
   */
  protected void onBeforeBuild(final Builder builder) {
  }

  public void onCachePeriodChanged(final long value) {
    managerInitializer.get().onCachePeriodChanged(value);
  }

  public void onModelPeriodChanged(final long value) {
    managerInitializer.get().onModelPeriodChanged(value);
  }

  public void destroy() {
    managerInitializer.destroy();
  }

  public void initialize(final StandaloneContext standaloneContext) {
    if (getOriginalDecoratedObject() instanceof StandaloneContextAware) {
      ((StandaloneContextAware) getOriginalDecoratedObject()).initialize(standaloneContext);
    }
  }
}
