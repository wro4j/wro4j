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
import ro.isdc.wro.model.resource.support.AbstractConfigurableStrategy;
import ro.isdc.wro.util.provider.ProviderFinder;


/**
 * Uses the {@link NamingStrategy} implementation associated with an alias read from properties file.
 * 
 * @author Alex Objelean
 * @created 17 Jun 2012
 * @since 1.4.7
 */
public class ConfigurableNamingStrategy
    extends AbstractConfigurableStrategy<NamingStrategy, NamingStrategyProvider>
    implements NamingStrategy {
  /**
   * Property name to specify namingStrategy alias.
   */
  public static final String PARAM_NAMING_STRATEGY = "namingStrategy";
  
  /**
   * {@inheritDoc}
   */
  public String rename(String originalName, InputStream inputStream)
      throws IOException {
    return getConfiguredStrategy().rename(originalName, inputStream);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected NamingStrategy getDefaultStrategy() {
    return new NoOpNamingStrategy();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, NamingStrategy> getStrategies(final NamingStrategyProvider provider) {
    return provider.provideNamingStrategies();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected String getStrategyKey() {
    return PARAM_NAMING_STRATEGY;
  }
}
