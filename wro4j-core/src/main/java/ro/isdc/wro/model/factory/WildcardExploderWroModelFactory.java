/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.factory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocatorDecorator;
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
  private static final Logger LOG = LoggerFactory.getLogger(WildcardExploderWroModelFactory.class);

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
        LOG.debug("resource uri: {}", resource.getUri());
        final UriLocator uriLocator = uriLocatorFactory.getInstance(resource.getUri());
        LOG.debug("uriLocator: " + uriLocator);
        if (uriLocator instanceof WildcardUriLocatorSupport) {
          final WildcardStreamLocator wildcardStreamLocator = ((WildcardUriLocatorSupport) uriLocator).getWildcardStreamLocator();
          if (wildcardStreamLocator.hasWildcard(resource.getUri())) {
            LOG.debug("Wildcard Explode for uri: " + resource.getUri());
            final WildcardStreamLocator decoratedWildcardStreamLocator = new WildcardStreamLocatorDecorator(wildcardStreamLocator) {
              @Override
              public void handleFoundFiles(final Collection<File> files) {
                LOG.debug("handleFoundFiles");
                final List<Resource> explodedResources = new ArrayList<Resource>();
                for (final File file : files) {
                  explodedResources.add(Resource.create("file:" + file.getAbsolutePath(), resource.getType()));
                }
                LOG.debug("replace resource {}", resource);
                group.replace(resource, explodedResources);
              }
            };

            //trigger the wildcard replacement
            try {
              LOG.debug("trigger wildcard replacement");
              final InvocationHandler handler = new InvocationHandler() {
                public Object invoke(final Object proxy, final Method method, final Object[] args)
                    throws Throwable {
                  LOG.debug("proxy handler invoke: " + proxy.getClass() + " arguments: " + Arrays.toString(args));
                  return uriLocator.locate((String) args[0]);
                }
              };
              final UriLocator proxy = (UriLocator) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {
                UriLocator.class
              }, handler);
              proxy.locate(resource.getUri());
            } catch (final IOException e) {
              //log only
              LOG.error("problem while trying to explode wildcard for the following resource uri: " + resource.getUri());
            }
          }
        }
      }
    }
    return model;
  }
}
