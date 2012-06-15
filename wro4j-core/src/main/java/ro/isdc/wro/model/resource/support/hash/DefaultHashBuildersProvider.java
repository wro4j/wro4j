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
 * @created 16 Jun 2012
 * @since 1.4.7
 */
public class DefaultHashBuildersProvider
    implements HashBuildersProvider {
  /**
   * {@inheritDoc}
   */
  public Map<String, HashBuilder> provideHashBuilders() {
    final Map<String, HashBuilder> map = new HashMap<String, HashBuilder>();
    map.put(CRC32HashBuilder.ALIAS, new CRC32HashBuilder());
    map.put(MD5HashBuilder.ALIAS, new MD5HashBuilder());
    map.put(SHA1HashBuilder.ALIAS, new SHA1HashBuilder());
    return map;
  }
  
}
