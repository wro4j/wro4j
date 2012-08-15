package ro.isdc.wro.model.resource.support.naming;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.support.AbstractConfigurableSingleStrategy;


/**
 * Uses the {@link NamingStrategy} implementation associated with an alias read from properties file.
 * 
 * @author Alex Objelean
 * @created 17 Jun 2012
 * @since 1.4.7
 */
public class ConfigurableNamingStrategy
    extends AbstractConfigurableSingleStrategy<NamingStrategy, NamingStrategyProvider>
    implements NamingStrategy {
  /**
   * Property name to specify namingStrategy alias.
   */
  public static final String KEY = "namingStrategy";
  @Inject
  private Injector injector;
  /**
   * {@inheritDoc}
   */
  public String rename(String originalName, InputStream inputStream)
      throws IOException {
    final NamingStrategy namingStrategy = getConfiguredStrategy();
    injector.inject(namingStrategy);
    return namingStrategy.rename(originalName, inputStream);
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
    return KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<NamingStrategyProvider> getProviderClass() {
    return NamingStrategyProvider.class;
  }
}
