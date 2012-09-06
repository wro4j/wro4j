package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.AutoCloseInputStream;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorAwareDecorator;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Responsible for injecting each {@link UriLocator} with required fields before being used.
 * 
 * @author Alex Objelean
 * @created 24 Apr 2012
 * @since 1.4.6
 */
public final class InjectorAwareUriLocatorFactoryDecorator
    extends InjectorAwareDecorator<UriLocatorFactory> implements UriLocatorFactory {
  
  public static UriLocatorFactory decorate(final UriLocatorFactory decorated, final Injector injector) {
    return decorated instanceof InjectorAwareUriLocatorFactoryDecorator ? decorated : new InjectorAwareUriLocatorFactoryDecorator(decorated, injector);
  }
  /**
   * Decorates an {@link UriLocatorFactory} and uses provided injector to inject the locator.
   * 
   * @param decorated
   * @param injector
   */
  private InjectorAwareUriLocatorFactoryDecorator(final UriLocatorFactory decorated, final Injector injector) {
    super(decorated, injector);
  }

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    final UriLocator instance = getDecoratedObject().getInstance(uri);
    if (instance != null) {
      getInjector().inject(instance);
    }
    return instance;
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream locate(final String uri)
      throws IOException {
    final UriLocator locator = getInstance(uri);
    if (locator == null) {
      throw new IOException("No locator is capable handling uri: " + uri);
    }
    return new AutoCloseInputStream(locator.locate(uri));
  }
}
