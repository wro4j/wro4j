package ro.isdc.wro.extensions.model.resource;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.AntPathMatcher;

import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.WroUtil;


/**
 * An implementation of {@link ResourceAuthorizationManager} which authorize the uri based on configured list of uri's
 * matched using antPath support.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class ConfigurableAntPathPatternsResourceAuthorizationManager
    implements ResourceAuthorizationManager {
  private final AntPathMatcher matcher = new AntPathMatcher();
  private List<String> patterns = new ArrayList<String>();

  @Override
  public boolean isAuthorized(final String uri) {
    for (final String pattern : patterns) {
      if (matcher.match(pattern, WroUtil.removeQueryString(uri))) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param list set a list of authorized patterns
   */
  public void setPatterns(final List<String> list) {
    notNull(list);
    this.patterns = list;
  }
}
