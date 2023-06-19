package ro.isdc.wro.model.resource.locator.factory;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * A decorator responsible to perform Injection every time
 * {@link InjectableUriLocatorFactoryDecorator#getInstance(String)} getInstance method is invoked.
 *
 * @author Alex Objelean
 * @since 1.7.4
 */
public class InjectableUriLocatorFactoryDecorator extends AbstractDecorator<UriLocatorFactory>
    implements UriLocatorFactory  {
  private static final Logger LOG = LoggerFactory.getLogger(InjectableUriLocatorFactoryDecorator.class);
  @Inject
  private Injector injector;
  public InjectableUriLocatorFactoryDecorator(final UriLocatorFactory decorated) {
    super(decorated);
  }

  /**
   * This implementation shows the problem with current design of locator implementation. Needs to be changed.
   */
  public InputStream locate(final String uri)
      throws IOException {
    final UriLocator locator = getInstance(uri);
    if (locator == null) {

      return getDecoratedObject().locate(uri);
    }
    return locator.locate(uri);
  }

  public UriLocator getInstance(final String uri) {
    final UriLocator instance = getDecoratedObject().getInstance(uri);
    if (instance != null) {
      LOG.debug("using {} locator for uri: {}", instance, uri);
      injector.inject(instance);
    }
    return instance;
  }
}
