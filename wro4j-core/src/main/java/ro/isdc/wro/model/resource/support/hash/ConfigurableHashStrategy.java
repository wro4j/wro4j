package ro.isdc.wro.model.resource.support.hash;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategy;


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
  @Inject
  private Injector injector;
  
  /**
   * {@inheritDoc}
   */
  public String getHash(final InputStream inputStream)
      throws IOException {
    final HashStrategy hashStrategy = getConfiguredStrategy();
    injector.inject(hashStrategy);
    return hashStrategy.getHash(inputStream);
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
