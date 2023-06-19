/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.manager.standalone;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.support.naming.DefaultHashEncoderNamingStrategy;


/**
 * An implementation of {@link WroModelFactory} which uses {@link DefaultHashEncoderNamingStrategy} for encoding the result
 * file name.
 *
 * @author Alex Objelean
 */
public class FingerprintAwareStandaloneManagerFactory extends ExtensionsStandaloneManagerFactory {
  public FingerprintAwareStandaloneManagerFactory() {
    setNamingStrategy(new DefaultHashEncoderNamingStrategy());
  }
}
