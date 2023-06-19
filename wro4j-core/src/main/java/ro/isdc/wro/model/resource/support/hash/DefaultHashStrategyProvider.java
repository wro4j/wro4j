package ro.isdc.wro.model.resource.support.hash;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.support.naming.NamingStrategy;
import ro.isdc.wro.model.resource.support.naming.NamingStrategyProvider;


/**
 * Default implementation of {@link NamingStrategyProvider} providing all {@link NamingStrategy} implementations from
 * core module.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public class DefaultHashStrategyProvider
    implements HashStrategyProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, HashStrategy> provideHashStrategies() {
    final Map<String, HashStrategy> map = new HashMap<String, HashStrategy>();
    map.put(CRC32HashStrategy.ALIAS, new CRC32HashStrategy());
    map.put(MD5HashStrategy.ALIAS, new MD5HashStrategy());
    map.put(SHA1HashStrategy.ALIAS, new SHA1HashStrategy());
    return map;
  }
  
}
