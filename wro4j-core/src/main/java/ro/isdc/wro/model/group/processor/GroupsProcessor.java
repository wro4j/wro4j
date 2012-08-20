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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.support.ProcessorsUtils;
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
  private WroModelFactory modelFactory;
  @Inject
  private WroConfiguration config;
  @Inject
  private Injector injector;
  
  /**
   * This field is transient because {@link PreProcessorExecutor} is not serializable (according to findbugs eclipse
   * plugin).
   */
  @Inject
  private transient PreProcessorExecutor preProcessorExecutor;
  
  /**
   * @param cacheKey
   *          to process.
   * @return processed content.
   */
  public String process(final CacheEntry cacheKey) {
    Validate.notNull(cacheKey);
    try {
      LOG.debug("Starting processing group [{}] of type [{}] with minimized flag: " + cacheKey.isMinimize(),
          cacheKey.getGroupName(), cacheKey.getType());
      // find processed result for a group
      final WroModel model = modelFactory.create();
      final Group group = model.getGroupByName(cacheKey.getGroupName());
      final Group filteredGroup = group.collectResourcesOfType(cacheKey.getType());
      if (filteredGroup.getResources().isEmpty()) {
        LOG.debug("No resources found in group: {} and resource type: {}", group.getName(), cacheKey.getType());
        if (!config.isIgnoreEmptyGroup()) {
          throw new WroRuntimeException("No resources found in group: " + group.getName());
        }
      }
      final String result = preProcessorExecutor.processAndMerge(filteredGroup.getResources(), cacheKey.isMinimize());
      return doPostProcess(result, cacheKey);
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources", e);
    } finally {
      callbackRegistry.onProcessingComplete();
    }
  }
  
  /**
   * Perform postProcessing.
   * 
   * @return the post processed contents.
   */
  private String doPostProcess(final String content, final CacheEntry cacheEntry)
      throws IOException {
    Validate.notNull(content);
    final Collection<ResourcePostProcessor> allPostProcessors = processorsFactory.getPostProcessors();
    if (allPostProcessors.isEmpty() && processorsFactory.getPreProcessors().isEmpty()) {
      LOG.debug("[WARN] No processors defined. Please, check if your configuration is correct.");
    }
    final Collection<ResourcePostProcessor> processors = ProcessorsUtils.filterProcessorsToApply(
        cacheEntry.isMinimize(), cacheEntry.getType(), allPostProcessors);
    return applyPostProcessors(processors, content);
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
    Reader reader = new StringReader(content.toString());
    Writer writer = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourcePostProcessor processor : processors) {
      stopWatch.start("Using " + processor.getClass().getSimpleName());
      writer = new StringWriter();
      try {
        callbackRegistry.onBeforePostProcess();
        decorateProcessor(processor).process(reader, writer);
      } finally {
        stopWatch.stop();
        callbackRegistry.onAfterPostProcess();
        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly(writer);
      }
      reader = new StringReader(writer.toString());
    }
    LOG.debug(stopWatch.prettyPrint());
    return writer.toString();
  }
  
  /**
   * @return a decorated postProcessor.
   */
  private ResourcePostProcessor decorateProcessor(final ResourcePostProcessor processor) {
    ResourcePostProcessor decorated = new ExceptionHandlingProcessorDecorator(processor);
    injector.inject(decorated);
    return decorated;
  }
}
