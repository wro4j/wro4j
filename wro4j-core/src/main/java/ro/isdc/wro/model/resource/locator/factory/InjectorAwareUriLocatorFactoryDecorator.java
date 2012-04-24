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
  }
  
  @Override
  public UriLocator getInstance(final String uri) {
    final UriLocator instance = super.getInstance(uri);
    //TODO avoid multiple inject call for the same instance?
    injector.inject(instance);
    return instance;
  }
  
  @Override
  public InputStream locate(String uri)
      throws IOException {
    return getInstance(uri).locate(uri);
  }
}
