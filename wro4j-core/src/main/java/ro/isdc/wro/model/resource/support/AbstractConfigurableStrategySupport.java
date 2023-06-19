package ro.isdc.wro.model.resource.support;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Abstracts the configurable creation of the strategies based on {@link ProviderFinder}. Uses two generic parameters:
 *
 * @param <S>
 *          strategy type
 * @param <P>
 *          provider type
 * @author Alex Objelean
 * @since 1.4.7
 */
public abstract class AbstractConfigurableStrategySupport<S, P> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigurableStrategySupport.class);

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
  public final Set<String> getAvailableAliases() {
    return getStrategyMap().keySet();
  }

  /**
   * @return a set of available strategies.
   */
  public final Collection<S> getAvailableStrategies() {
    return getStrategyMap().values();
  }

  /**
   * @return the map where the strategy alias will be searched. By default a {@link ProviderFinder} is used to build the
   *         map. If two providers have same alias, the one with higher order will override those with lower order.
   */
  private final Map<String, S> newStrategyMap() {
    //preserve the order
    final Map<String, S> map = new LinkedHashMap<String, S>();
    final List<P> providers = getProviderFinder().find();
    //reverse before adding to map, to allow highest ordered override lowest one (assuming same alias is used).
    Collections.reverse(providers);
    LOG.debug("providers: {}", providers);
    for (final P provider : providers) {
      try {
        map.putAll(getStrategies(provider));
      } catch (final Exception e) {
        LOG.warn("Could not load strategies for provider: {}, because of: {}. It will be skipped.", provider,
            e.getMessage());
      }
    }
    // allow client to override defaults
    overrideDefaultStrategyMap(map);
    return map;
  }

  /**
   * Invoked after the the map of strategies is built from providers. Allows client to override its keys or add new
   * entries.
   *
   * @param map
   *          the built map.
   */
  protected void overrideDefaultStrategyMap(final Map<String, S> map) {
  }

  /**
   * Utility method which copies all entries from source into target. Entries with the same keys from target will be
   * overridden with entries from source. This operation is similar to {@link Map#putAll(Map)}, but it doesn't require
   * changing generics to wildcards.
   *
   * @param source
   *          the map from where the entries will be copied into target.
   * @param target
   *          the map where to put entries from source.
   */
  protected final void copyAll(final Map<String, S> source, final Map<String, S> target) {
    notNull(source);
    notNull(target);
    for (final Map.Entry<String, S> entry : source.entrySet()) {
      target.put(entry.getKey(), entry.getValue());
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
   * This method is not final only for testing purposes.
   * @return the {@link ProviderFinder} used to find all strategies.
   */
  protected ProviderFinder<P> getProviderFinder() {
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
