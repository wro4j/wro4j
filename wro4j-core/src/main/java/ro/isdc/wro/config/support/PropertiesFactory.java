package ro.isdc.wro.config.support;

import java.util.Properties;

import ro.isdc.wro.util.ObjectFactory;


/**
 * Meant for internal usage only. This is similar to {@link ObjectFactory}, but with a different name since it should be
 * implemented by a class which already implements {@link ObjectFactory}.
 *
 * @author Alex Objelean
 * @since 1.7.1
 */
public interface PropertiesFactory {
  /**
   * @return {@link Properties} object built by implemented class.
   */
  Properties createProperties();
}
