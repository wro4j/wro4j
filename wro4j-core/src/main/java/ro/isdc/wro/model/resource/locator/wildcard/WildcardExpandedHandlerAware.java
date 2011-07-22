/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import java.io.File;
import java.util.Collection;

import ro.isdc.wro.util.Transformer;


/**
 * Classes implementing this interface will be able to get a WildcardExpanderHandler, which is an interface capable of
 * doing something with the files associated with one wildcard resource element.
 *
 * @author Alex Objelean
 * @created 20 Jul 2011
 * @since 1.3.9
 */
public interface WildcardExpandedHandlerAware {
  /**
   * Sets the handler to be used by the implementing class.
   *
   * @param handler a {@link Transformer} which does the handler job. The {@link Transformer} is not the ideal interface
   *        to be used, but it can be a temporary solution until a better one is found.
   */
  void setWildcardExpanderHandler(Transformer<Collection<File>> handler);
}
