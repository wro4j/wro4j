package ro.isdc.wro.extensions.model.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.util.AntPathMatcher;

import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;


/**
 * An implementation of {@link ResourceAuthorizationManager} which authorize the uri based on configured list of uri's
 * matched using antPath support.
 * 
 * @author Alex Objelean
 * @since 1.5.0
 * @created 22 Sep 2012
 */
public class ConfigurableAntPathPatternsResourceAuthorizationManager
    implements ResourceAuthorizationManager {
  private AntPathMatcher matcher = new AntPathMatcher();
  private List<String> patterns = new ArrayList<String>();
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAuthorized(final String uri) {
    for (String pattern : patterns) {
      if (matcher.match(pattern, uri)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param list set a list of authorized patterns
   */
  public void setPatterns(final List<String> list) {
    Validate.notNull(list);
    this.patterns = list;
  }
}
