package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Responsible for injecting each {@link UriLocator} with required fields before being used.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public final class InjectorAwareUriLocatorFactoryDecorator
    extends UriLocatorFactoryDecorator {
  private final Injector injector;
  
  /**
   * Decorates an {@link UriLocatorFactory} and uses provided injector to inject the locator.
   * 
   * @param decorated
   * @param injector
   */
  public InjectorAwareUriLocatorFactoryDecorator(final UriLocatorFactory decorated, final Injector injector) {
    super(decorated);
    Validate.notNull(injector);
    this.injector = injector;
    inject(decorated);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UriLocator getInstance(final String uri) {
    final UriLocator instance = super.getInstance(uri);
    //TODO avoid multiple inject call for the same instance?
    if (instance != null) {
      injector.inject(instance);
    }
    return instance;
  }
  
  /**
   * Handles injection by inspecting decorated factories if required. 
   */
  private void inject(final UriLocatorFactory locatorFactory) {
    Validate.notNull(locatorFactory);
    injector.inject(locatorFactory);
    if (locatorFactory instanceof UriLocatorFactoryDecorator) {
      injector.inject(((UriLocatorFactoryDecorator) locatorFactory).getDecoratedObject());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream locate(final String uri)
      throws IOException {
    final UriLocator locator = getInstance(uri);
    if (locator == null) {
      throw new IOException("No locator is capable handling uri: " + uri);
    }
    return locator.locate(uri);
  }
}
