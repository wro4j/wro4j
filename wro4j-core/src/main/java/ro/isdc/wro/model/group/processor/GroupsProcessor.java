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
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
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
  @Inject
  private Injector injector;
  @Inject
  private WroConfiguration config;
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
    try {
      //mark this group as used.
      group.markAsUsed();
      final Group filteredGroup = group.collectResourcesOfType(type);
      if (filteredGroup.getResources().isEmpty()) {
        LOG.warn("No resources found in group: {} and resource type: {}", group.getName(), type);
        if (!config.isIgnoreEmptyGroup()) {
          throw new WroRuntimeException("No resources found in group: " + group.getName());
        }
      }
      final String result = decorateWithMergeCallback(preProcessorExecutor).processAndMerge(
          filteredGroup.getResources(), minimize);
      return doPostProcess(type, result, minimize);
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources", e);
    } finally {
      callbackRegistry.onProcessingComplete();
    }
  }

  /**
   * @return decorated {@link PreProcessorExecutor} which add callback calls.
   */
  private PreProcessorExecutor decorateWithMergeCallback(final PreProcessorExecutor executor) {
    return new PreProcessorExecutor() {
      @Override
      public String processAndMerge(final List<Resource> resources, final boolean minimize) throws IOException {
        callbackRegistry.onBeforeMerge();
        try {
          return executor.processAndMerge(resources, minimize);
        } finally {
          callbackRegistry.onAfterMerge();
        }
      }
    };
  }

  /**
   * Perform postProcessing.
   *
   * @param resourceType
   *          the type of the resources to process. This value will never be null.
   * @param content
   *          the merged content of all resources which were pre-processed.
   * @param minimize
   *          whether minimize aware post processor must be applied.
   * @return the post processed contents.
   */
  private String doPostProcess(final ResourceType resourceType, final String content, final boolean minimize)
      throws IOException {
    Validate.notNull(content);
    final Collection<ResourcePostProcessor> allPostProcessors = processorsFactory.getPostProcessors();
    if (allPostProcessors.isEmpty() && processorsFactory.getPreProcessors().isEmpty()) {
      LOG.warn("No processors defined. Please, check if your configuration is correct.");
    }
    final Collection<ResourcePostProcessor> processors = ProcessorsUtils.filterProcessorsToApply(minimize, resourceType, allPostProcessors);
    final String output = applyPostProcessors(processors, content);
    return output;
  }

  /**
   * Apply resourcePostProcessors.
   *
   * @param processors
   *          a collection of processors to apply on the content from the supplied writer.
   * @param content
   *          to process with all postProcessors.
   * @return the post processed content.
   */
  private String applyPostProcessors(final Collection<ResourcePostProcessor> processors, final String content)
      throws IOException {
    LOG.debug("postProcessors: {}", processors);
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content.toString());
    Writer output = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourcePostProcessor processor : processors) {
      stopWatch.start("Using " + processor.getClass().getSimpleName());
      //inject all required properites
      injector.inject(processor);

      output = new StringWriter();
      decorateWithPostProcessCallback(processor).process(input, output);

      input = new StringReader(output.toString());
      stopWatch.stop();
    }
    LOG.debug(stopWatch.prettyPrint());
    return output.toString();
  }


  /**
   * @return a decorated postProcessor which invokes callback methods.
   */
  private ResourcePostProcessor decorateWithPostProcessCallback(final ResourcePostProcessor processor) {
    return new ResourcePostProcessor() {
      public void process(final Reader reader, final Writer writer)
          throws IOException {
        // TODO update callbackContext
        callbackRegistry.onBeforePostProcess();
        try {
          processor.process(reader, writer);
        } finally {
          callbackRegistry.onAfterPostProcess();
        }
      }
    };
  }
}
