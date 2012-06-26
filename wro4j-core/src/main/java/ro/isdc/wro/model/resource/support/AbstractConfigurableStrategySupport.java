package ro.isdc.wro.model.resource.support;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Abstracts the configurable creation of the strategies based on {@link ProviderFinder}. Uses two generic parameters:
 * 
 * @param <S>
 *          strategy type
 * @param <P>
 *          provider type
 * @author Alex Objelean
 * @created 26 Jun 2012
 * @since 1.4.7
 */
public abstract class AbstractConfigurableStrategySupport<S, P> {
  private ProviderFinder<P> providerFinder;
  private Map<String, S> map;
  private Properties properties;
  
  /**
   * Override this method if a fallback search is required.
   * 
   * @param alias
   *          of the strategy to use.
   * @return the strategy S if one is found or null if no corresponding strategy found.
   */
  protected S getStrategyForAlias(final String alias) {
    return getStrategyMap().get(alias);
  }
  
  /**
   * @return the value configured in the properties file using the strategy key.
   */
  protected final String getConfiguredValue() {
    return getProperties().getProperty(getStrategyKey());
  }
  
  /**
   * @return a set of available aliases.
   */
  protected final Set<String> getAvailableAliases() {
    return getStrategyMap().keySet();
  }
  
  /**
   * @return a set of available strategies.
   * @VisibleForTesting
   */
  public final Collection<S> getAvailableStrategies() {
    return getStrategyMap().values();
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
  
  /**
   * Allows the original strategy map to be overriden with new values.
   * 
   * @param overrideMap
   *          a not null map of values to be added (or overriden) to original strategy map.
   */
  protected final void addToStrategyMap(final Map<String, S> overrideMap) {
    Validate.notNull(map);
    for (Map.Entry<String, S> entry : overrideMap.entrySet()) {
      map.put(entry.getKey(), entry.getValue());
    }
  }

  private Map<String, S> getStrategyMap() {
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
      providerFinder = ProviderFinder.of(getProviderClass());
    }
    return providerFinder;
  }
  
  /**
   * @return the class of the provider of type P. Uses {@link ParameterizedType} to compute the class. Override it to
   *         support anonymous classes which do not play well with {@link ParameterizedType}'s.
   */
  @SuppressWarnings("unchecked")
  protected Class<P> getProviderClass() {
    final Type type = getClass().getGenericSuperclass();
    return (Class<P>) ((ParameterizedType) type).getActualTypeArguments()[1];
  }
  
  /**
   * @param provider
   *          the instance responsible for strategy lookup.
   * @return the map of provided strategies.
   */
  protected abstract Map<String, S> getStrategies(final P provider);

  /**
   * @return the key of the strategy property.
   */
  protected abstract String getStrategyKey();
}
