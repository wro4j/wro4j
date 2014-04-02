package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * Uses the {@link HashStrategy} implementation associated with an alias read from properties file.
 *
 * @author Alex Objelean
 * @created 17 Jun 2012
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
  public String getHash(final InputStream inputStream)
      throws IOException {
    try{
      return getConfiguredStrategy().getHash(inputStream);
	}finally{
      IOUtils.closeQuietly(inputStream);
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
