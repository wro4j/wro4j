/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.model.transformer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardExpandedHandlerAware;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.util.Transformer;


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
 * This model transformation is also known as wildcard expander, because it mutates the model after it is built by
 * adding resources to the group which contains resources with wildcard uri.
 *
 * @author Alex Objelean
 * @created 18 Jul 2011
 * @since 1.4.0
 */
public class WildcardExpanderModelTransformer
  implements Transformer<WroModel> {
  private static final Logger LOG = LoggerFactory.getLogger(WildcardExpanderModelTransformer.class);

  @Inject
  private UriLocatorFactory uriLocatorFactory;
  @Inject
  private DuplicateResourceDetector duplicateResourceDetector;


  /**
   * {@inheritDoc}
   */
  public WroModel transform(final WroModel input) {
    final WroModel model = input;

    for (final Group group : model.getGroups()) {
      final List<Resource> resources = group.getResources();
      for (final Resource resource : resources) {
        final UriLocator uriLocator = uriLocatorFactory.getInstance(resource.getUri());

        if (uriLocator instanceof WildcardUriLocatorSupport) {
          final WildcardStreamLocator wildcardStreamLocator = ((WildcardUriLocatorSupport)uriLocator).getWildcardStreamLocator();

          if (wildcardStreamLocator.hasWildcard(resource.getUri())
            && wildcardStreamLocator instanceof WildcardExpandedHandlerAware) {

            final WildcardExpandedHandlerAware expandedHandler = (WildcardExpandedHandlerAware)wildcardStreamLocator;
            LOG.debug("expanding resource uri: {}", resource.getUri());

            final String baseNameFolder = computeBaseNameFolder(resource, uriLocator, expandedHandler);

            // force the reset of the detector to avoid situations when resources are considered duplicates in unit
            // tests.
            duplicateResourceDetector.reset();

            expandedHandler.setWildcardExpanderHandler(createExpanderHandler(group, resource, baseNameFolder));
            try {
              // trigger the wildcard replacement
              uriLocator.locate(resource.getUri());
            } catch (final IOException e) {
              // log only
              LOG.error("problem while trying to expand wildcard for the following resource uri: {}", resource.getUri());
            } finally {
              // remove the handler, it is not needed anymore
              expandedHandler.setWildcardExpanderHandler(null);
            }
          }
        }
      }
    }
    LOG.debug("Transformed model: {}", model);
    return model;
  }


  /**
   * @param resource
   * @param uriLocator
   * @param expandedHandler
   */
  private String computeBaseNameFolder(final Resource resource, final UriLocator uriLocator,
    final WildcardExpandedHandlerAware expandedHandler) {
    // Find the baseName
    // add a simple wildcard to trigger the wildcard detection
    final String resourcePath = FilenameUtils.getPath(resource.getUri())
      + DefaultWildcardStreamLocator.RECURSIVE_WILDCARD;
    LOG.debug("resourcePath: {}", resourcePath);
    // use thread local because we need to assign a File inside an anonymous class and it fits perfectly
    final ThreadLocal<String> baseNameFolderHolder = new ThreadLocal<String>();
    expandedHandler.setWildcardExpanderHandler(new Transformer<Collection<File>>() {
      public Collection<File> transform(final Collection<File> input)
        throws Exception {
        LOG.debug("expanded Files: " + input);
        for (final File file : input) {
          baseNameFolderHolder.set(file.getParent());
          // no need to continue
          break;
        }
        // us this to skip wildcard stream detection, we are only interested in the baseName
        throw new IOException("Skip further wildcard processing");
      }
    });

    try {
      uriLocator.locate(resourcePath);
    } catch (final Exception e) {
      LOG.error("problem while trying to get basePath for : {}", resourcePath);
    }
    return baseNameFolderHolder.get();
  }


  /**
   * create the handler which expand the resources containing wildcard.
   */
  public Transformer<Collection<File>> createExpanderHandler(final Group group, final Resource resource,
    final String baseNameFolder) {
    LOG.debug("createExpanderHandler using baseNameFolder {} for resource {}", baseNameFolder, resource);
    final Transformer<Collection<File>> handler = new Transformer<Collection<File>>() {
      public Collection<File> transform(final Collection<File> files) {
        if (baseNameFolder == null) {
          //replacing group with empty list since the original uri has no associated resources.
          LOG.debug("No BaseNameFolder found, replacing group with empty list");
          group.replace(resource, new ArrayList<Resource>());
        } else {
          final List<Resource> expandedResources = new ArrayList<Resource>();
          for (final File file : files) {
            final String resourcePath = FilenameUtils.getPathNoEndSeparator(resource.getUri());
            LOG.debug("resourcePath: {}", resourcePath);

            final String computedResourceUri = resourcePath
              + StringUtils.removeStart(file.getPath(), baseNameFolder).replace('\\', '/');

            final Resource expandedResource = Resource.create(computedResourceUri, resource.getType());
            LOG.debug("expanded resource: {}", expandedResource);
            expandedResources.add(expandedResource);
          }
          LOG.debug("replace resource {}", resource);
          group.replace(resource, expandedResources);

        }
        // Because there is actually no transformation, here it doesn't matter what we return.
        return null;
      }
    };
    return handler;
  }
}
