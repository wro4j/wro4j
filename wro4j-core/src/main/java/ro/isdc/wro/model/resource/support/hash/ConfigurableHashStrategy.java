package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * Uses the {@link HashStrategy} implementation associated with an alias read from properties file.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class ConfigurableHashStrategy
    extends AbstractConfigurableSingleStrategy<HashStrategy, HashStrategyProvider>
    implements HashStrategy {
  /**
   * Property name to specify alias.
   */
  public static final String KEY = "hashStrategy";

  /**
   * {@inheritDoc}
   */
  @Override
  public String getHash(final InputStream inputStream)
      throws IOException {
    try (inputStream) {
      return getConfiguredStrategy().getHash(inputStream);
	}
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getStrategyKey() {
    return KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected HashStrategy getDefaultStrategy() {
    return new SHA1HashStrategy();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Map<String, HashStrategy> getStrategies(final HashStrategyProvider provider) {
    return provider.provideHashStrategies();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<HashStrategyProvider> getProviderClass() {
    return HashStrategyProvider.class;
  }
}
