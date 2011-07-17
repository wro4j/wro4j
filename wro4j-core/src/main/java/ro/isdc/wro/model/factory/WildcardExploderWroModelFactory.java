/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.factory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;

/**
 * A decorator which looks up for resources containing wildcards and replaces them with the corresponding collection of
 * specific resources. For example, a resource of this type:
 * <p/>
 * {@code
 *   <js>/path/to/**.js</js>
 * }
 * <p/>
 * can be transformed into:
 * <p/>
 * {@code
 *   <js>/path/to/a1.js</js>
 * } <br/>
 * {@code
 *   <js>/path/to/a2.js</js>
 * } <br/>
 * {@code
 *   <js>/path/to/a3.js</js>
 * } <br/>
 * <p/>
 * This model transformation is also known as model exploder, because it mutates the model after it is built by adding
 * resources to the group which contains resources with wildcard uri.
 *
 * @author Alex Objelean
 * @created 18 Jul 2011
 * @since 1.3.9
 */
public class WildcardExploderWroModelFactory extends WroModelFactoryDecorator {
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  public WildcardExploderWroModelFactory(final WroModelFactory decorated) {
    super(decorated);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WroModel create() {
    final WroModel model = super.create();

    for (final Group group : model.getGroups()) {
      for (final Resource resource : group.getResources()) {
        final UriLocator uriLocator = uriLocatorFactory.getInstance(resource.getUri());
        if (uriLocator instanceof WildcardUriLocatorSupport) {
          final WildcardStreamLocator wildcardStreamLocator = ((WildcardUriLocatorSupport) uriLocator).getWildcardStreamLocator();
          if (wildcardStreamLocator.hasWildcard(resource.getUri())) {
            //TODO replace wildcard resource with exploded resources
          }
        }
      }
    }
    return model;
  }
}
