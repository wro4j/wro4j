package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Apply all preProcessor on provided {@link Resource} and returns the result of execution as String.
 * <p>
 * This is useful when you want to preProcess a resource which is not a part of the model (css import use-case).
 *
 * @author Alex Objelean
 */
public class PreProcessorExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(PreProcessorExecutor.class);
  private final AbstractGroupsProcessor groupsProcessor;


  /**
   * Constructor.
   *
   * @param groupsProcessor
   *          to set.
   */
  public PreProcessorExecutor(final AbstractGroupsProcessor groupsProcessor) {
    this.groupsProcessor = groupsProcessor;
  }

  /**
   * Execute all the preProcessors on the given resource.
   *
   * @param resource {@link Resource} to preProcess.
   * @param minimize whether the minimimize aware preProcessor must be applied.
   * @return the result of preProcessing as string content.
   * @throws IOException if {@link Resource} cannot be found or any other related errors.
   */
  public String execute(final Resource resource, final boolean minimize)
      throws IOException {
    if (resource == null) {
      throw new IllegalArgumentException("Resource cannot be null!");
    }
    // merge preProcessorsBy type and anyPreProcessors
    final Collection<ResourcePreProcessor> processors = groupsProcessor.getPreProcessorsByType(resource.getType());
    processors.addAll(groupsProcessor.getPreProcessorsByType(null));
    if (!minimize) {
      GroupsProcessorImpl.removeMinimizeAwareProcessors(processors);
    }
    return applyPreProcessors(processors, resource);
  }


  /**
   * Apply a list of preprocessors on a resource.
   *
   * @throws IOException
   *           if any IO error occurs while processing.
   */
  private String applyPreProcessors(final Collection<ResourcePreProcessor> processors, final Resource resource)
      throws IOException {
    // get original content
    Reader reader = null;
    Writer output = new StringWriter();
    try {
      reader = groupsProcessor.getResourceReader(resource);
    } catch (final IOException e) {
      LOG.debug("IgnoreMissingResources: " + groupsProcessor.isIgnoreMissingResources());
      if (groupsProcessor.isIgnoreMissingResources()) {
        LOG.warn("Invalid resource found: " + resource);
        return output.toString();
      } else {
        LOG.warn("Invalid resource found: " + resource + ". Cannot continue processing. IgnoreMissingResources is + "
          + groupsProcessor.isIgnoreMissingResources());
        throw e;
      }
    }
    if (processors.isEmpty()) {
      IOUtils.copy(reader, output);
      return output.toString();
    }
    Reader input = reader;
    for (final ResourcePreProcessor processor : processors) {
      output = new StringWriter();
      LOG.debug("applying preProcessor: " + processor.getClass().getName());
      processor.process(resource, input, output);
      input = new StringReader(output.toString());
    }
    return output.toString();
  }
}