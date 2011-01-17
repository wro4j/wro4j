package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.encoding.SmartEncodingInputStream;


/**
 * TODO: refactor this class.
 * Apply all preProcessor on provided {@link Resource} and returns the result of execution as String.
 * <p>
 * This is useful when you want to preProcess a resource which is not a part of the model (css import use-case).
 *
 * @author Alex Objelean
 */
public abstract class PreProcessorExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(PreProcessorExecutor.class);
  private final UriLocatorFactory uriLocatorFactory;
  private final DuplicateResourceDetector duplicateResourceDetector;


  public PreProcessorExecutor(final UriLocatorFactory uriLocatorFactory, final DuplicateResourceDetector duplicateResourceDetector) {
    if (uriLocatorFactory == null) {
      throw new IllegalArgumentException("uriLocatorFactory cannot be null!");
    }
    if (duplicateResourceDetector == null) {
      throw new IllegalArgumentException("duplicateResourceDetector cannot be null!");
    }
    this.uriLocatorFactory = uriLocatorFactory;
    this.duplicateResourceDetector = duplicateResourceDetector;
  }
  /**
   * Apply preProcessors on resources and merge them.
   *
   * @param resources what are the resources to merge.
   * @param minimize whether minimize aware processors must be applied or not.
   * @return preProcessed merged content.
   * @throws IOException if IO error occurs while merging.
   */
  public String processAndMerge(final List<Resource> resources, final boolean minimize)
    throws IOException {
    final StringBuffer result = new StringBuffer();
    for (final Resource resource : resources) {
      LOG.debug("merging resource: " + resource);
      result.append(processSingleResource(resource, resources, minimize));
    }
    return result.toString();
  }

  /**
   * Execute all the preProcessors on the provided resource.
   *
   * @param resource
   *          {@link Resource} to preProcess.
   * @param resources
   *          the list of all resources to be processed in this context.
   * @param minimize
   *          whether the minimize aware preProcessor must be applied.
   * @return the result of preProcessing as string content.
   */
  private String processSingleResource(final Resource resource, final List<Resource> resources, final boolean minimize)
    throws IOException {
    //TODO: hold a list of processed resources in order to avoid duplicates

    // merge preProcessorsBy type and anyPreProcessors
    final Collection<ResourcePreProcessor> processors = getPreProcessorsByType(resource.getType());
    if (!minimize) {
      GroupsProcessor.removeMinimizeAwareProcessors(processors);
    }
    return applyPreProcessors(resource, resources, processors);
  }


  /**
   * TODO: refactor this method.
   * <p/>
   * Apply a list of preprocessors on a resource.
   *
   * @param resource the {@link Resource} on which processors will be applied
   * @param resources the list of all resources to be processed in this context.
   * @param processors the list of processor to apply on the resource.
   */
  private String applyPreProcessors(final Resource resource, final List<Resource> resources, final Collection<ResourcePreProcessor> processors)
    throws IOException {
    //TODO close reader & writer?
    Reader reader = null;
    try {
      try {
        reader = getResourceReader(resource, resources);
      } catch (final IOException e) {
        LOG.warn("Invalid resource found: " + resource);
        if (ignoreMissingResources()) {
          return "";
        } else {
          LOG.warn("Cannot continue processing. IgnoreMissingResources is + " + ignoreMissingResources());
          throw e;
        }
      }
      if (processors.isEmpty()) {
        return IOUtils.toString(reader);
      }
      Writer writer = null;
      for (final ResourcePreProcessor processor : processors) {
        writer = new StringWriter();
        // skip minimize validation if resource doesn't want to be minimized
        final boolean applyProcessor = resource.isMinimize()
          || !processor.getClass().isAnnotationPresent(Minimize.class);
        if (applyProcessor) {
          LOG.debug("PreProcessing - " + processor.getClass().getSimpleName());
          processor.process(resource, reader, writer);
        } else {
          IOUtils.copy(reader, writer);
          LOG.debug("skipped processing on resource: " + resource);
        }
        reader.close();
        reader = new StringReader(writer.toString());
      }
      return writer.toString();
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * @param resources
   *          the list of all resources to be processed in this context. This is necessary in order to detect
   *          duplicates.
   * @return a Reader for the provided resource.
   */
  private Reader getResourceReader(final Resource resource, final List<Resource> resources)
      throws IOException {
    InputStream is = null;
    try {
      // populate duplicate Resource detector with known used resource uri's
      for (final Resource r : resources) {
        duplicateResourceDetector.addResourceUri(r.getUri());
      }
      is = uriLocatorFactory.locate(resource.getUri());
      return new InputStreamReader(new SmartEncodingInputStream(is));
    } finally {
      duplicateResourceDetector.reset();
    }
  }

  /**
   * @return true if the missing resources should be ignored.
   */
  protected abstract boolean ignoreMissingResources();

  /**
   * @param resourceType type of searched resources.
   * @return a collection of {@link ResourcePreProcessor}'s by resourceType.
   */
  protected abstract Collection<ResourcePreProcessor> getPreProcessorsByType(ResourceType resourceType);

}