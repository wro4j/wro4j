/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
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
  private ProcessorsFactory processorsFactory;
  /**
   * This field is transient because {@link PreProcessorExecutor} is not serializable (according to findbugs eclipse
   * plugin).
   */
  @Inject
  private transient PreProcessorExecutor preProcessorExecutor;

  /**
   * {@inheritDoc}
   * <p>
   * While processing the resources, if any exception occurs - it is wrapped in a RuntimeException.
   */
  public String process(final Group group, final ResourceType type, final boolean minimize) {
    Validate.notNull(group);
    Validate.notNull(type);

	final StopWatch stopWatch = new StopWatch();
    stopWatch.start("filter resources");
    // TODO find a way to reuse contents from cache
    filterResources(group, type);
    try {
      stopWatch.stop();
      stopWatch.start("pre process and merge");
      // Merge
      final String result = preProcessorExecutor.processAndMerge(group, minimize);
      stopWatch.stop();
        
      stopWatch.start("post process");
      // postProcessing
      final String postProcessedResult = applyPostProcessors(group, type, result, minimize);
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
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
  private String applyPostProcessors(final Group group, final ResourceType resourceType, final String content, final boolean minimize)
    throws IOException {
    Validate.notNull(content);
    final Collection<ResourceProcessor> allPostProcessors = processorsFactory.getPostProcessors();
    if (allPostProcessors.isEmpty() && processorsFactory.getPreProcessors().isEmpty()) {
      LOG.warn("No processors defined. Please, check if your configuration is correct.");
    }
    Collection<ResourceProcessor> processors = ProcessorsUtils.getProcessorsByType(resourceType, allPostProcessors);
    processors.addAll(ProcessorsUtils.getProcessorsByType(null, allPostProcessors));
    if (!minimize) {
      processors = ProcessorsUtils.getMinimizeFreeProcessors(processors);
    }

    String resourceName = group.getName() + "." + resourceType.name().toLowerCase();
    Resource mergedResource = Resource.create(resourceName, resourceType);
    mergedResource.setMinimize(false);
    mergedResource.setType(resourceType);
    
    LOG.debug("postProcessors: {}", processors);
    final String output = applyPostProcessors(mergedResource, processors, content);
    return output;
  }


  /**
   * Apply resourcePostProcessors.
   *
   * @param processors a collection of processors to apply on the content from the supplied writer.
   * @param content to process with all postProcessors.
   * @return the post processed content.
   */
  private String applyPostProcessors(final Resource mergedResource, final Collection<ResourceProcessor> processors, final String content)
    throws IOException {
    if (processors.isEmpty()) {
      return content;
    }
    
    Reader input = new StringReader(content.toString());
    Writer output = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourceProcessor processor : processors) {
      stopWatch.start("Using " + processor.getClass().getSimpleName());
      output = new StringWriter();
      processor.process(mergedResource, input, output);
      input = new StringReader(output.toString());
      stopWatch.stop();
    }
    LOG.debug(stopWatch.prettyPrint());
    return output.toString();
  }

  /**
   * @param groups list of groups where to search resources to filter.
   * @param type of resources to collect.
   * @return a list of resources of provided type.
   */
  private final void filterResources(final Group group, final ResourceType type) {
    final List<Resource> allResources = new ArrayList<Resource>();
    allResources.addAll(group.getResources());

    // retain only resources of needed type
    final List<Resource> filteredResources = new ArrayList<Resource>();
    for (final Resource resource : allResources) {
      if (type == resource.getType()) {
        if (filteredResources.contains(resource)) {
          LOG.warn("Duplicated resource detected: " + resource + ". This resource won't be included more than once!");
        } else {
          filteredResources.add(resource);
        }
      }
    }
    
    group.setResources(filteredResources);
  }
}
