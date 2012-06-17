package ro.isdc.wro.model.resource.support.naming;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.ProcessorProvider;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Uses the {@link NamingStrategy} implementation associated with an alias read from properties file.
 * 
 * @author Alex Objelean
 * @created 17 Jun 2012
 * @since 1.4.7
 */
public class ConfigurableNamingStrategy
    implements NamingStrategy {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableNamingStrategy.class);
  
  /**
   * Property name to specify namingStrategy alias.
   */
  public static final String PARAM_NAMING_STRATEGY = "namingStrategy";
  
  private ProviderFinder<NamingStrategyProvider> providerFinder;
  private Map<String, NamingStrategy> map;
  private Properties properties;
  
  /**
   * {@inheritDoc}
   */
  public String rename(String originalName, InputStream inputStream)
      throws IOException {
    return getConfiguredNamingStrategy().rename(originalName, inputStream);
  }
  
  /**
   * @return the {@link NamingStrategy} whose alias is found configured in the properties. This method will never return
   *         null. If no alias is defined the {@link NoOpNamingStrategy} will be returned. If an invalid alias is
   *         provided - a runtime exception will be thrown.
   * @VisibleForTesting
   */
  NamingStrategy getConfiguredNamingStrategy() {
    final String alias = getProperties().getProperty(PARAM_NAMING_STRATEGY);
    NamingStrategy namingStrategy = new NoOpNamingStrategy(); 
    if (!StringUtils.isEmpty(alias)) {
      LOG.debug("configured alias: {}", alias);
      namingStrategy = getNamingStrategyMap().get(alias);
      if (namingStrategy == null) {
        throw new WroRuntimeException("Invalid namingStrategy alias provided: <" + alias + ">. Available aliases are: "
            + getNamingStrategyMap().keySet());
      }
    }
    LOG.debug("using NamingStrategy: {}", namingStrategy);
    return namingStrategy;
  }

  /**
   * @return the map where the {@link NamingStrategy} alias will be searched. By default a {@link ProviderFinder} is
   *         used to build the map.
   */
  protected Map<String, NamingStrategy> newNamingStrategyMap() {
    final Map<String, NamingStrategy> map = new HashMap<String, NamingStrategy>();
    final List<NamingStrategyProvider> providers = getProviderFinder().find();
    for (NamingStrategyProvider provider : providers) {
      map.putAll(provider.provideNamingStrategies());
    }
    return map;
  }

  private Map<String, NamingStrategy> getNamingStrategyMap() {
    if (map == null) {
      map = newNamingStrategyMap();
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
   * @return reference to this {@link ConfigurableNamingStrategy}.
   */
  public ConfigurableNamingStrategy setProperties(final Properties props) {
    Validate.notNull(props);
    this.properties = props;
    return this;
  }

  /**
   * @VisibleForTesting
   * @return the {@link ProviderFinder} used to find all {@link ProcessorProvider}'s.
   */
  ProviderFinder<NamingStrategyProvider> getProviderFinder() {
    if (providerFinder == null) {
      providerFinder = ProviderFinder.of(NamingStrategyProvider.class);
    }
    return providerFinder;
  }
}
