/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager.standalone;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.support.naming.HashEncoderNamingStrategy;


/**
 * An implementation of {@link WroModelFactory} which uses {@link HashEncoderNamingStrategy} for encoding the result
 * file name.
 *
 * @author Alex Objelean
 * @created 15 Aug 2010
 */
public class FingerprintAwareStandaloneManagerFactory extends ExtensionsStandaloneManagerFactory {
  public FingerprintAwareStandaloneManagerFactory() {
    setNamingStrategy(new HashEncoderNamingStrategy());
  }
}
