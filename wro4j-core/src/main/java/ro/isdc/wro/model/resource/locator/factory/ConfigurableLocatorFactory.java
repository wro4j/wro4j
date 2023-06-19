/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.support.LocatorProvider;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.support.AbstractConfigurableMultipleStrategy;


/**
 * A {@link ProcessorsFactory} implementation which is easy to configure using a {@link Properties} object.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public class ConfigurableLocatorFactory
    extends AbstractConfigurableMultipleStrategy<UriLocator, LocatorProvider>
    implements UriLocatorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurableLocatorFactory.class);
  /**
   * Name of init param used to specify uri locators.
   */
  public static final String PARAM_URI_LOCATORS = "uriLocators";

  private final UriLocatorFactory locatorFactory = newLocatorFactory();

  @Override
  protected String getStrategyKey() {
    return PARAM_URI_LOCATORS;
  }

  @Override
  protected Map<String, UriLocator> getStrategies(final LocatorProvider provider) {
    return provider.provideLocators();
  }

  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    return new AutoCloseInputStream(locatorFactory.locate(uri));
  }

  /**
   * {@inheritDoc}
   */
  private UriLocatorFactory newLocatorFactory() {
    final SimpleUriLocatorFactory factory = new SimpleUriLocatorFactory();
    final List<UriLocator> locators = getConfiguredStrategies();
    for (final UriLocator locator : locators) {
      factory.addLocator(locator);
    }
    // use default when none provided
    if (locators.isEmpty()) {
      LOG.debug("No locators configured. Using Default locator factory.");
      return new DefaultUriLocatorFactory();
    }
    return factory;
  }

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    return locatorFactory.getInstance(uri);
  }

  @Override
  protected Class<LocatorProvider> getProviderClass() {
    return LocatorProvider.class;
  }
}
