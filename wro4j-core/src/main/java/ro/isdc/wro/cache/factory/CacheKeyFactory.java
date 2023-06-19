package ro.isdc.wro.cache.factory;

import jakarta.servlet.http.HttpServletRequest;
import ro.isdc.wro.cache.CacheKey;

/**
 * Factory responsible for creating {@link CacheKey}
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public interface CacheKeyFactory {
  /**
   * Creates a {@link CacheKey} from the provided request.
   * @param request {@link HttpServletRequest} object used to build {@link CacheKey} from.
   * @return not null {@link CacheKey} object or null if the {@link CacheKey} could not be built.
   */
  CacheKey create(final HttpServletRequest request);
}
