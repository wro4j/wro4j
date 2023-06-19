/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.util.Collection;

import ro.isdc.wro.util.Function;


/**
 * Classes implementing this interface will be able to get a WildcardExpanderHandler, which is an interface capable of
 * doing something with the files associated with one wildcard resource element.
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
public interface WildcardExpanderHandlerAware {
  /**
   * Sets the handler to be used by the implementing class.
   *
   * @param handler a {@link Function} which does the handler job.
   */
  void setWildcardExpanderHandler(Function<Collection<File>, Void> handler);
}
