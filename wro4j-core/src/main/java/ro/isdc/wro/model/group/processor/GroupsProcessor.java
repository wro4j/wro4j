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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.StopWatch;


/**
 * Default group processor which perform preProcessing, merge and postProcessing on groups resources.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class GroupsProcessor {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(GroupsProcessor.class);
  @Inject
  private ProcessorsFactory processorsFactory;
  @Inject
  private DuplicateResourceDetector duplicateResourceDetector;
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
  public String process(final Collection<Group> groups, final ResourceType type, final boolean minimize) {
    if (groups == null) {
      throw new IllegalArgumentException("List of groups cannot be null!");
    }
    if (type == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }

    //Supress spurious duplicate resource detection on reload
    duplicateResourceDetector.reset();
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("filter resources");
    // TODO find a way to reuse contents from cache
    final List<Resource> filteredResources = getFilteredResources(groups, type);
    try {
      stopWatch.stop();
      stopWatch.start("pre process and merge");
      // Merge
      final String result = preProcessorExecutor.processAndMerge(filteredResources, minimize);
      stopWatch.stop();
      stopWatch.start("post process");
      // postProcessing
      final String postProcessedResult = applyPostProcessors(type, result, minimize);
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
  private String applyPostProcessors(final ResourceType resourceType, final String content, final boolean minimize)
    throws IOException {
    if (content == null) {
      throw new IllegalArgumentException("content cannot be null!");
    }
    final Collection<ResourcePostProcessor> allPostProcessors = processorsFactory.getPostProcessors();
    Collection<ResourcePostProcessor> processors = ProcessorsUtils.getProcessorsByType(resourceType, allPostProcessors);
    processors.addAll(ProcessorsUtils.getProcessorsByType(null, allPostProcessors));
    if (!minimize) {
      processors = ProcessorsUtils.getMinimizeFreeProcessors(processors);
    }
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
    for (final ResourcePostProcessor processor : processors) {
      output = new StringWriter();
      LOG.debug("PostProcessing - " + processor.getClass().getSimpleName());
      processor.process(input, output);
      input = new StringReader(output.toString());
    }
    return output.toString();
  }

  /**
   * @param groups list of groups where to search resources to filter.
   * @param type of resources to collect.
   * @return a list of resources of provided type.
   */
  private final List<Resource> getFilteredResources(final Collection<Group> groups, final ResourceType type) {
    final List<Resource> allResources = new ArrayList<Resource>();
    for (final Group group : groups) {
      allResources.addAll(group.getResources());
    }
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
    return filteredResources;
  }
}
