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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.wildcard.DefaultWildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardExpanderHandlerAware;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardStreamLocator;
import ro.isdc.wro.model.resource.locator.wildcard.WildcardUriLocatorSupport;
import ro.isdc.wro.util.Function;
import ro.isdc.wro.util.Transformer;

/**
 * <p>A decorator which looks up for resources containing wildcards and replaces them with the corresponding collection of
 * specific resources. For example, a resource of this type:</p>
 *
 * {@code
 *   <js>/path/to/**.js</js>
 * }
 *
 * <p>can be transformed into:</p>
 *
 * {@code
 *   <js>/path/to/a1.js</js>
 * } <br/>
 * {@code
 *   <js>/path/to/a2.js</js>
 * } <br/>
 * {@code
 *   <js>/path/to/a3.js</js>
 * } <br/>
 *
 * <p>This model transformation is also known as wildcard expander, because it mutates the model after it is built by
 * adding resources to the group which contains resources with wildcard uri.</p>
 *
 * @author Alex Objelean
 * @since 1.4.0
 */
@SuppressWarnings("serial")
public class WildcardExpanderModelTransformer
    implements Transformer<WroModel> {
  private static final Logger LOG = LoggerFactory.getLogger(WildcardExpanderModelTransformer.class);

  @Inject
  private UriLocatorFactory locatorFactory;

  /**
   * An instance of IOException having a special purpose: to skip subsequent attempts to localize a stream.
   */
  public static class NoMoreAttemptsIOException
      extends IOException {
    public NoMoreAttemptsIOException(final String message) {
      super(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public synchronized WroModel transform(final WroModel model) {

    for (final Group group : model.getGroups()) {
      final List<Resource> resources = group.getResources();
      for (final Resource resource : resources) {
        processResource(group, resource);
      }
    }
    LOG.debug("Transformed model: {}", model);
    return model;
  }

  /**
   * Process each resource and replace it with a collection of resources if it contains wildcard.
   */
  private void processResource(final Group group, final Resource resource) {
    final UriLocator uriLocator = locatorFactory.getInstance(resource.getUri());

    if (uriLocator instanceof WildcardUriLocatorSupport) {
      final WildcardStreamLocator wildcardStreamLocator = ((WildcardUriLocatorSupport) uriLocator).getWildcardStreamLocator();

      // TODO should we probably handle the situation when wildcard is present, but the implementation is not
      // expandedHandledAware?
      if (wildcardStreamLocator.hasWildcard(resource.getUri())
          && wildcardStreamLocator instanceof WildcardExpanderHandlerAware) {

        final WildcardExpanderHandlerAware expandedHandler = (WildcardExpanderHandlerAware) wildcardStreamLocator;
        LOG.debug("Expanding resource: {}", resource.getUri());

        final String baseNameFolder = computeBaseNameFolder(resource, uriLocator, expandedHandler);
        LOG.debug("baseNameFolder: {}", baseNameFolder);

        expandedHandler.setWildcardExpanderHandler(createExpanderHandler(group, resource, baseNameFolder));
        try {
          // trigger the wildcard replacement
          uriLocator.locate(resource.getUri());
        } catch (final IOException e) {
          // log only
          LOG.debug("[FAIL] problem while trying to expand wildcard for the following resource uri: {}",
              resource.getUri());
        } finally {
          // remove the handler, it is not needed anymore
          expandedHandler.setWildcardExpanderHandler(null);
        }
      }
    }
  }

  /**
   * Computes the file name of the folder where the resource is located. The implementation uses a trick by invoking the
   * {@link WildcardExpanderHandlerAware} to get the baseName.
   */
  private String computeBaseNameFolder(final Resource resource, final UriLocator uriLocator,
      final WildcardExpanderHandlerAware expandedHandler) {
    // Find the baseName
    // add a recursive wildcard to trigger the wildcard detection. The simple wildcard ('*') is not enough because it
    // won't work for folders containing only directories with no files.
    LOG.debug("computeBaseNameFolder for resource {}", resource);
    final String resourcePath = FilenameUtils.getFullPath(resource.getUri())
        + DefaultWildcardStreamLocator.RECURSIVE_WILDCARD;
    LOG.debug("resourcePath: {}", resourcePath);
    // use thread local because we need to assign a File inside an anonymous class and it fits perfectly
    final ThreadLocal<String> baseNameFolderHolder = new ThreadLocal<String>();
    expandedHandler.setWildcardExpanderHandler(createBaseNameComputerFunction(baseNameFolderHolder));

    try {
      uriLocator.locate(resourcePath);
    } catch (final Exception e) {
      LOG.debug("[FAIL] Exception caught during wildcard expanding for resource: {}\n with exception message {}",
          resourcePath, e.getMessage());
    }
    if (baseNameFolderHolder.get() == null) {
      LOG.debug("[FAIL] Cannot compute baseName folder for resource: {}", resource);
    }
    return baseNameFolderHolder.get();
  }

  private Function<Collection<File>, Void> createBaseNameComputerFunction(final ThreadLocal<String> baseNameFolderHolder) {
    return new Function<Collection<File>, Void>() {
      public Void apply(final Collection<File> input)
          throws Exception {
        LOG.debug("\texpanded Files: {}", input);
        for (final File file : input) {
          LOG.debug("\tsetting baseNameFolder: {}", file.getParent());
          baseNameFolderHolder.set(file.getParent());
          // no need to continue
          break;
        }
        // use this to skip wildcard stream detection, we are only interested in the baseName
        throw new NoMoreAttemptsIOException("BaseNameFolder computed successfully, skip further wildcard processing..");
      }
    };
  }

  /**
   * create the handler which expand the resources containing wildcard.
   */
  public Function<Collection<File>, Void> createExpanderHandler(final Group group, final Resource resource,
      final String baseNameFolder) {
    LOG.debug("createExpanderHandler using baseNameFolder: {}\n for resource {}", baseNameFolder, resource);
    return new Function<Collection<File>, Void>() {
      public Void apply(final Collection<File> files) {
        if (baseNameFolder == null) {
          // replacing group with empty list since the original uri has no associated resources.
          // No BaseNameFolder found
          LOG.warn("The resource {} is probably invalid, removing it from the group.", resource);
          group.replace(resource, new ArrayList<Resource>());
        } else {
          final List<Resource> expandedResources = new ArrayList<Resource>();
          LOG.debug("baseNameFolder: {}", baseNameFolder);
          for (final File file : files) {
            final String resourcePath = getFullPathNoEndSeparator(resource);
            String filePath = file.getPath();
            String resourcePathWithoutProtocol;
            final String computedResourceUri;

            LOG.debug("\tResource path: {}", resourcePath);
            LOG.debug("\tFile path: {}", filePath);

            if (resourcePath.startsWith(ClasspathUriLocator.PREFIX)) {
              resourcePathWithoutProtocol = resourcePath.replaceFirst(ClasspathUriLocator.PREFIX, StringUtils.EMPTY);
            } else {
              resourcePathWithoutProtocol = resourcePath;
            }

            LOG.debug("\tResource path without protocol: {}", resourcePathWithoutProtocol);

            String baseNameTrimmedFromResourcePath;

            if (StringUtils.isEmpty(resourcePathWithoutProtocol)) {
              baseNameTrimmedFromResourcePath = baseNameFolder;
            } else {
              baseNameTrimmedFromResourcePath = baseNameFolder.replaceFirst(".*" + resourcePathWithoutProtocol,
                  StringUtils.EMPTY);
            }

            LOG.debug("\tBase folder name trimmed from resource path: {}", baseNameTrimmedFromResourcePath);

            String filePathWithoutStart = StringUtils.removeStart(filePath, baseNameTrimmedFromResourcePath)
                .replace('\\', '/');

            if (StringUtils.isEmpty(resourcePathWithoutProtocol)) {
              computedResourceUri = resourcePath + filePathWithoutStart;
            } else {
              computedResourceUri = resourcePath
                  + filePathWithoutStart.replaceFirst(".*" + resourcePathWithoutProtocol, StringUtils.EMPTY);
            }

            LOG.debug("\tComputed resource URI: {}", computedResourceUri);

            final Resource expandedResource = Resource.create(computedResourceUri, resource.getType());
            LOG.debug("\texpanded resource: {}", expandedResource);
            expandedResources.add(expandedResource);
          }
          LOG.debug("\treplace resource {}", resource);
          group.replace(resource, expandedResources);
        }
        return null;
      }

      /**
       * This method fixes the problem when a resource in a group uses deep wildcard and starts at the root.
       * <p/>
       * Find more details <a href="https://github.com/alexo/wro4j/pull/44">here</a>.
       */
      private String getFullPathNoEndSeparator(final Resource resource1) {
        final String result = FilenameUtils.getFullPathNoEndSeparator(resource1.getUri());
        if (result != null && 1 == result.length() && 0 == FilenameUtils.indexOfLastSeparator(result)) {
          return "";
        }

        return result;
      }
    };
  }
}
