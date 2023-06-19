package ro.isdc.wro.model.resource.support.naming;

import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of {@link NamingStrategyProvider} providing all {@link NamingStrategy} implementations from
 * core module.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class DefaultNamingStrategyProvider
    implements NamingStrategyProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, NamingStrategy> provideNamingStrategies() {
    final Map<String, NamingStrategy> map = new HashMap<String, NamingStrategy>();
    map.put(TimestampNamingStrategy.ALIAS, new TimestampNamingStrategy());
    map.put(NoOpNamingStrategy.ALIAS, new NoOpNamingStrategy());
    map.put(DefaultHashEncoderNamingStrategy.ALIAS, new DefaultHashEncoderNamingStrategy());
    map.put(FolderHashEncoderNamingStrategy.ALIAS, new FolderHashEncoderNamingStrategy());
    return map;
  }
}
