package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.StopWatch;


/**
 * TODO: refactor this class. Apply all preProcessor on provided {@link Resource} and returns the result of execution as
 * String.
 * <p>
 * This is useful when you want to preProcess a resource which is not a part of the model (css import use-case).
 *
 * @author Alex Objelean
 */
public final class PreProcessorExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(PreProcessorExecutor.class);
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  @Inject
  private DuplicateResourceDetector duplicateResourceDetector;
  @Inject
  private ProcessorsFactory processorsFactory;


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
   * @param resource {@link Resource} to preProcess.
   * @param resources the list of all resources to be processed in this context.
   * @param minimize whether the minimize aware preProcessor must be applied.
   * @return the result of preProcessing as string content.
   */
  private String processSingleResource(final Resource resource, final List<Resource> resources, final boolean minimize)
    throws IOException {
    // TODO: hold a list of processed resources in order to avoid duplicates
    // merge preProcessorsBy type and anyPreProcessors
    Collection<ResourcePreProcessor> processors = ProcessorsUtils.getProcessorsByType(resource.getType(),
      processorsFactory.getPreProcessors());
    if (!minimize) {
      processors = ProcessorsUtils.getMinimizeFreeProcessors(processors);
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
  private String applyPreProcessors(final Resource resource, final List<Resource> resources,
    final Collection<ResourcePreProcessor> processors)
    throws IOException {
    String resourceContent = getResourceContent(resource, resources);
    if (processors.isEmpty()) {
      return resourceContent;
    }
    Writer writer = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourcePreProcessor processor : processors) {
      stopWatch.start("Using " + processor.getClass().getSimpleName());
      writer = new StringWriter();
      // skip minimize validation if resource doesn't want to be minimized
      final boolean applyProcessor = resource.isMinimize() || !processor.getClass().isAnnotationPresent(Minimize.class);
      if (applyProcessor) {
        LOG.debug("PreProcessing - " + processor.getClass().getSimpleName());
        final Reader reader = new StringReader(resourceContent);
        try {
          processor.process(resource, reader, writer);
        } catch (final IOException e) {
          if (!Context.get().getConfig().isIgnoreMissingResources()) {
            throw e;
          }
        }
      } else {
        writer.write(resourceContent);
        LOG.debug("skipped processing on resource: " + resource);
      }
      resourceContent = writer.toString();
      stopWatch.stop();
    }
    LOG.debug(stopWatch.prettyPrint());
    return writer.toString();
  }

  /**
   * @return a Reader for the provided resource.
   * @param resource
   *          {@link Resource} which content to return.
   * @param resources
   *          the list of all resources processed in this context, used for duplicate resource detection.
   */
  private String getResourceContent(final Resource resource, final List<Resource> resources)
    throws IOException {
    final WroConfiguration config = Context.get().getConfig();
    try {
      // populate duplicate Resource detector with known used resource uri's
      for (final Resource r : resources) {
        duplicateResourceDetector.addResourceUri(r.getUri());
      }
      final InputStream is = new BOMInputStream(uriLocatorFactory.locate(resource.getUri()));
      final String result = IOUtils.toString(is, config.getEncoding());
      is.close();
      return result;
    } catch (final IOException e) {
      LOG.warn("Invalid resource found: " + resource);
      if (config.isIgnoreMissingResources()) {
        return StringUtils.EMPTY;
      } else {
        LOG.error("Cannot ignore the missing resource:  " + resource);
        throw e;
      }
    } finally {
      duplicateResourceDetector.reset();
    }
  }
}