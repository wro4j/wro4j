package ro.isdc.wro.model.resource.support;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.util.DestroyableLazyInitializer;
import ro.isdc.wro.util.LazyInitializer;


/**
 * Abstracts the configurable creation of the strategies. Responsible for configuration of a single strategy. If no
 * configured strategy is found, a default one is used.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public abstract class AbstractConfigurableSingleStrategy<S, P>
    extends AbstractConfigurableStrategySupport<S, P> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurableSingleStrategy.class);
  @Inject
  private Injector injector;
  private final LazyInitializer<S> lazyInitializer = new DestroyableLazyInitializer<S>() {
    @Override
    protected S initialize() {
      final String alias = getConfiguredValue();
      S configuredStrategy = getDefaultStrategy();
      if (!StringUtils.isEmpty(alias)) {
        LOG.debug("configured alias: {}", alias);
        configuredStrategy = getStrategyForAlias(alias);
        if (configuredStrategy == null) {
          throw new WroRuntimeException("Invalid strategy alias provided: <" + alias + ">. Available aliases are: "
              + getAvailableAliases());
        }
      }
      // inject only when injector is available.
      if (injector != null) {
        injector.inject(configuredStrategy);
      }
      LOG.debug("using strategy: {}", configuredStrategy);
      return configuredStrategy;
    }
  };

  /**
   * The returned object will be injected (if injector is available) and will be cached, meaning that only the first
   * invocation of this method will instantiate a fresh strategy and any subsequent invocation will return the same
   * object.
   *
   * @return the strategy S whose alias is found configured in the properties. This method will never return null. If no
   *         alias is defined the default strategy will be returned. If an invalid alias is provided - a runtime
   *         exception will be thrown.
   */
  public final S getConfiguredStrategy() {
    return lazyInitializer.get();
  }

  /**
   * @return the default strategy implementation to use when no key is configured is provided.
   */
  protected abstract S getDefaultStrategy();
}
