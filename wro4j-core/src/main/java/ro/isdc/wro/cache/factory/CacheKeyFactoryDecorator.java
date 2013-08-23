package ro.isdc.wro.cache.factory;

import javax.servlet.http.HttpServletRequest;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Decorator for {@link CacheKeyFactory} object.
 *
 * @author Alex Objelean
 * @since 1.6.0
 * @created 19 Oct 2012
 */
public class CacheKeyFactoryDecorator
    extends AbstractDecorator<CacheKeyFactory>
    implements CacheKeyFactory {
  public CacheKeyFactoryDecorator(final CacheKeyFactory decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  public CacheKey create(final HttpServletRequest request) {
    return getDecoratedObject().create(request);
  }
}
