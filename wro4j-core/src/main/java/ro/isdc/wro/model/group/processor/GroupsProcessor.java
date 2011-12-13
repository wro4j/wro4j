/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.StopWatch;


/**
 * Default group processor which perform preProcessing, merge and postProcessing on groups resources.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class GroupsProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(GroupsProcessor.class);
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private ProcessorsFactory processorsFactory;
  /**
   * This field is transient because {@link PreProcessorExecutor} is not serializable (according to findbugs eclipse
   * plugin).
   */
  @Inject
  private transient PreProcessorExecutor preProcessorExecutor;

  /**
   * While processing the resources, if any exception occurs - it is wrapped in a RuntimeException.
   */
  public String process(final Group group, final ResourceType type, final boolean minimize) {
    Validate.notNull(group);
    Validate.notNull(type);

    // TODO find a way to reuse contents from cache
    final Group filteredGroup = group.collectResourcesOfType(type);
    try {
      callbackRegistry.onBeforeMerge();
      // Merge
      final String result = preProcessorExecutor.processAndMerge(filteredGroup.getResources(), minimize);
      
      callbackRegistry.onAfterMerge();
      
      // postProcessing
      final String postProcessedResult = doPostProcess(type, result, minimize);
      
      callbackRegistry.onProcessingComplete();
      return postProcessedResult;
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources", e);
    }
  }


  /**
   * Perform postProcessing.
   *
   * @param resourceType the type of the resources to process. This value will never be null.
   * @param content the merged content of all resources which were pre-processed.
   * @param minimize whether minimize aware post processor must be applied.
   * @return the post processed contents.
   */
  private String doPostProcess(final ResourceType resourceType, final String content, final boolean minimize)
    throws IOException {
    Validate.notNull(content);
    final Collection<ResourcePostProcessor> allPostProcessors = processorsFactory.getPostProcessors();
    if (allPostProcessors.isEmpty() && processorsFactory.getPreProcessors().isEmpty()) {
      LOG.warn("No processors defined. Please, check if your configuration is correct.");
    }
    Collection<ResourcePostProcessor> processors = ProcessorsUtils.getProcessorsByType(resourceType, allPostProcessors);
    processors.addAll(ProcessorsUtils.getProcessorsByType(null, allPostProcessors));
    if (!minimize) {
      processors = ProcessorsUtils.getMinimizeFreeProcessors(processors);
    }
    LOG.debug("postProcessors: {}", processors);
    final String output = applyPostProcessors(processors, content);
    return output;
  }


  /**
   * Apply resourcePostProcessors.
   *
   * @param processors a collection of processors to apply on the content from the supplied writer.
   * @param content to process with all postProcessors.
   * @return the post processed content.
   */
  private String applyPostProcessors(final Collection<ResourcePostProcessor> processors, final String content)
    throws IOException {
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content.toString());
    Writer output = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourcePostProcessor processor : processors) {
      stopWatch.start("Using " + processor.getClass().getSimpleName());
      output = new StringWriter();

      //TODO update callbackContext
      callbackRegistry.onBeforePostProcess();
      processor.process(input, output);
      callbackRegistry.onAfterPostProcess();

      input = new StringReader(output.toString());
      stopWatch.stop();
    }
    LOG.debug(stopWatch.prettyPrint());
    return output.toString();
  }
}
