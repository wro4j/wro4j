package ro.isdc.wro.model.resource.support;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * Abstracts the configurable creation of the strategies. Responsible for configuration of a single strategy. If no
 * configured strategy is found, a default one is used.
 * 
 * @author Alex Objelean
 * @created 17 Jun 2012
 * @since 1.4.7
 */
public abstract class AbstractConfigurableSingleStrategy<S, P>
    extends AbstractConfigurableStrategySupport<S, P> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurableSingleStrategy.class);

  /**
   * @return the strategy S whose alias is found configured in the properties. This method will never return null. If no
   *         alias is defined the {@link SHA1HashBuilder} will be returned. If an invalid alias is provided - a runtime
   *         exception will be thrown.
   * @VisibleForTesting
   */
  public final S getConfiguredStrategy() {
    final String alias = getConfiguredValue();
    S strategy = getDefaultStrategy();
    if (!StringUtils.isEmpty(alias)) {
      LOG.debug("configured alias: {}", alias);
      strategy = getStrategyForAlias(alias);
      if (strategy == null) {
        throw new WroRuntimeException("Invalid strategy alias provided: <" + alias + ">. Available aliases are: "
            + getAvailableAliases());
      }
    }
    LOG.debug("using strategy: {}", strategy);
    return strategy;
  }
  /**
   * @return the default strategy implementation to use when no key is configured is provided.
   */
  protected abstract S getDefaultStrategy();
}
