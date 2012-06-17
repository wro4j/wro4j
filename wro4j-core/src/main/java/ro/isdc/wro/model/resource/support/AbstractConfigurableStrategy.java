package ro.isdc.wro.model.resource.support;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.support.hash.HashStrategy;
import ro.isdc.wro.model.resource.support.hash.SHA1HashBuilder;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Abstracts the configurable creation of the strategies. Uses two generic parameters:
 * 
 * @param <S>
 *          strategy type
 * @param <P>
 *          provider type
 * @author Alex Objelean
 * @created 17 Jun 2012
 * @since 1.4.7
 */
public abstract class AbstractConfigurableStrategy<S, P> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurableStrategy.class);
  
  private ProviderFinder<P> providerFinder;
  private Map<String, S> map;
  private Properties properties;
  private Class<P> providerClass;

  @SuppressWarnings("unchecked")
  public AbstractConfigurableStrategy() {
    Type type = getClass().getGenericSuperclass();
    providerClass = (Class<P>) ((ParameterizedType) type).getActualTypeArguments()[1];
  }
  
  /**
   * @return the {@link HashStrategy} whose alias is found configured in the properties. This method will never return
   *         null. If no alias is defined the {@link SHA1HashBuilder} will be returned. If an invalid alias is provided
   *         - a runtime exception will be thrown.
   * @VisibleForTesting
   */
  public final S getConfiguredStrategy() {
    final String alias = getProperties().getProperty(getStrategyKey());
    S strategy = getDefaultStrategy();
    if (!StringUtils.isEmpty(alias)) {
      LOG.debug("configured alias: {}", alias);
      strategy = getNamingStrategyMap().get(alias);
      if (strategy == null) {
        throw new WroRuntimeException("Invalid strategy alias provided: <" + alias + ">. Available aliases are: "
            + getNamingStrategyMap().keySet());
      }
    }
    LOG.debug("using strategy: {}", strategy);
    return strategy;
  }
  
  /**
   * @return the map where the strategy alias will be searched. By default a {@link ProviderFinder} is used to build the
   *         map.
   */
  protected Map<String, S> newStrategyMap() {
    final Map<String, S> map = new HashMap<String, S>();
    final List<P> providers = getProviderFinder().find();
    for (P provider : providers) {
      map.putAll(getStrategies(provider));
    }
    return map;
  }
  
  private Map<String, S> getNamingStrategyMap() {
    if (map == null) {
      map = newStrategyMap();
    }
    return map;
  }
  
  /**
   * To be used for internal usage. Ensure that returned object is not null.
   */
  private Properties getProperties() {
    if (this.properties == null) {
      this.properties = newProperties();
    }
    return this.properties;
  }
  
  /**
   * @return {@link Properties} used to lookup namingStrategy alias.
   */
  protected Properties newProperties() {
    return new Properties();
  }
  
  /**
   * @param props
   *          {@link Properties} containing configured alias.
   */
  public final void setProperties(final Properties props) {
    Validate.notNull(props);
    this.properties = props;
  }
  
  /**
   * @VisibleForTesting
   * @return the {@link ProviderFinder} used to find all strategies.
   */
  protected final ProviderFinder<P> getProviderFinder() {
    if (providerFinder == null) {
      providerFinder = ProviderFinder.of(providerClass);
    }
    return providerFinder;
  }
  
  /**
   * @param provider
   *          the instance responsible for strategy lookup.
   * @return the map of provided strategies.
   */
  protected abstract Map<String, S> getStrategies(final P provider);
  
  /**
   * @return the default strategy implementation to use when no key is configured is provided.
   */
  protected abstract S getDefaultStrategy();
  
  /**
   * @return the key of the strategy property.
   */
  protected abstract String getStrategyKey();
}
