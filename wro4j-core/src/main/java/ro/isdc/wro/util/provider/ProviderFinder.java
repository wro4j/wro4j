package ro.isdc.wro.util.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.Ordered;


/**
 * Helps to find available providers of any supported type.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class ProviderFinder<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ProviderFinder.class);

  private final Class<T> type;

  /**
   * @VisibleForTesting.
   * @param type
   *          the type of providers to find.
   */
  ProviderFinder(final Class<T> type) {
    this.type = type;
  }

  /**
   * Creates a {@link ProviderFinder} which will find providers of type provided as argument..
   *
   * @param type
   *          the type of providers to search.
   * @return {@link ProviderFinder} handling providers lookup.
   */
  public static <T> ProviderFinder<T> of(final Class<T> type) {
    return new ProviderFinder<T>(type);
  }

  /**
   * @return the list of all providers found in classpath. The returned list is sorted according to the {@link Ordered} interface (from {@link Ordered#LOWEST} to {@link Ordered#HIGHEST}.
   */
  public List<T> find() {
    final List<T> providers = new ArrayList<T>();
    try {
      final Iterator<T> iterator = lookupProviders(type);
      for (; iterator.hasNext();) {
        final T provider = iterator.next();
        LOG.debug("found provider: {}", provider);
        providers.add(provider);
      }
      collectConfigurableProviders(providers);
    } catch (final Exception e) {
      LOG.error("Failed to discover providers using ServiceRegistry. Cannot continue...", e);
      throw WroRuntimeException.wrap(e);
    }
    Collections.sort(providers, Ordered.DESCENDING_COMPARATOR);
    LOG.debug("found providers: {}", providers);
    return providers;
  }

  /**
   * Collects also providers of type {@link ConfigurableProvider} if the T type is a supertype of
   * {@link ConfigurableProvider}. If the type is already {@link ConfigurableProvider}, it will be ignored to avoid
   * adding of duplicate providers.
   *
   * @param providers
   *          the list where found providers will be added.
   */
  @SuppressWarnings("unchecked")
  private void collectConfigurableProviders(final List<T> providers) {
    if (type.isAssignableFrom(ConfigurableProvider.class) && (type != ConfigurableProvider.class)) {
      final Iterator<ConfigurableProvider> iterator = lookupProviders(ConfigurableProvider.class);
      for (; iterator.hasNext();) {
        final T provider = (T) iterator.next();
        LOG.debug("found provider: {}", provider);
        providers.add(provider);
      }
    }
  }

  /**
   * This method is useful for mocking the lookup operation. The implementation will try to use java.util.ServiceLoader
   * by default (available in jdk6) and will fallback to ServiceRegistry for earlier JDK versions. The reason for this
   * is to support GAE environment which doesn't contain the ServiceRegistry in its whitelist.
   *
   * @param providerClass
   *          the class of the provider to lookup.
   * @VisibleForTesting
   * @return the iterator of found providers.
   */
  @SuppressWarnings("unchecked")
  <P> Iterator<P> lookupProviders(final Class<P> providerClass) {
    LOG.debug("searching for providers of type : {}", providerClass);
    try {
      final Class<?> serviceLoader = getClass().getClassLoader().loadClass("java.util.ServiceLoader");
      LOG.debug("using {} to lookupProviders", serviceLoader.getName());
      return ((Iterable<P>) serviceLoader.getMethod("load", Class.class).invoke(serviceLoader, providerClass)).iterator();
    } catch (final Exception e) {
      LOG.debug("ServiceLoader is not available. Falling back to ServiceRegistry.", e);
    }
    LOG.debug("using {} to lookupProviders", ServiceRegistry.class.getName());
    return ServiceRegistry.lookupProviders(providerClass);
  }
}
