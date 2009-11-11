/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;

/**
 * Default group processor which perform preProcessing, merge and postProcessing
 * on groups resources.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public final class GroupsProcessorImpl extends AbstractGroupsProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(GroupsProcessorImpl.class);

  /**
   * {@inheritDoc} While processing the resources, if any exception occurs - it
   * is wrapped in a RuntimeException.
   */
  public String process(final List<Group> groups, final ResourceType type) {
    if (groups == null) {
      throw new NullPointerException("List of groups cannot be null!");
    }
    if (type == null) {
      throw new NullPointerException("ResourceType cannot be null!");
    }
    String result = null;
    final List<Resource> filteredResources = getFilteredResources(groups, type);
    try {
      // Merge
      result = preProcessAndMerge(filteredResources);

      // postProcessing
      if (ResourceType.CSS == type) {
        result = applyPostProcessors(getCssPostProcessors(), result);
      } else {
        result = applyPostProcessors(getJsPostProcessors(), result);
      }
      result = applyPostProcessors(this.getAnyResourcePostProcessors(), result);
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources", e);
    }
    return result;
  }

  /**
   * Apply preProcess and merge the resource collection into the writer.
   *
   * @param resources
   *          what are the resources to merge.
   * @param writer
   *          where to write the merged resources.
   * @return Writer where the merged resources are written.
   */
  private String preProcessAndMerge(final List<Resource> resources)
      throws IOException {
    log.debug("<preProcessAndMerge>");

    final StringBuffer result = new StringBuffer();
    for (final Resource resource : resources) {
      log.debug("\tmerging: " + resource);
      String preProcessedContent = null;
      // get original content
      final Reader reader = resource.getReader();
      final String originalContent = IOUtils.toString(reader);
      reader.close();

      // preProcessing
      final String resourceUri = resource.getUri();
      if (ResourceType.CSS == resource.getType()) {
        preProcessedContent = applyPreProcessors(resourceUri,
            getCssPreProcessors(), originalContent);
      } else {
        preProcessedContent = applyPreProcessors(resourceUri,
            getJsPreProcessors(), originalContent);
      }
      preProcessedContent = applyPreProcessors(resourceUri,
          getAnyResourcePreProcessors(), preProcessedContent);
      result.append(preProcessedContent);
    }
    log.debug("</preProcessAndMerge>");
    return result.toString();
  }

  /**
   * Apply resourcePreProcessors.
   *
   * @param resourceUri
   *          uri of the resource. This property is used by preProcessor.
   * @param processors
   *          a list of processor to apply on the content from the supplied
   *          writer.
   * @param content
   *          The content to preProcess as String.
   * @return the processed content as String.
   */
  private String applyPreProcessors(final String resourceUri,
      final List<ResourcePreProcessor> processors, final String content)
      throws IOException {
    if (processors == null) {
      throw new NullPointerException("Processors list cannot be null!");
    }
    if (content == null) {
      throw new NullPointerException("content cannot be null!");
    }
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content);
    Writer output = null;
    for (final ResourcePreProcessor processor : processors) {
      log.debug("<apply> " + processor);
      output = new StringWriter();
      processor.process(resourceUri, input, output);
      input = new StringReader(output.toString());
    }
    return output.toString();
  }

  /**
   * Apply resourcePostProcessors.
   *
   * @param processors
   *          a list of processor to apply on the content from the supplied
   *          writer.
   * @param content
   *          to process with all postProcessors.
   * @return Writer the processed content is written to this writer.
   */
  private String applyPostProcessors(
      final List<ResourcePostProcessor> processors, final String content)
      throws IOException {
    if (processors == null) {
      throw new NullPointerException("Processors list cannot be null!");
    }
    if (content == null) {
      throw new NullPointerException("Writer cannot be null!");
    }
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content.toString());
    Writer output = null;
    for (final ResourcePostProcessor processor : processors) {
      log.debug("<apply> " + processor);
      output = new StringWriter();
      processor.process(input, output);
      input = new StringReader(output.toString());
    }
    return output.toString();
  }

  /**
   * @param groups
   *          list of groups where to search resources to filter.
   * @param type
   *          of resources to collect.
   * @return a list of resources of provided type.
   */
  private List<Resource> getFilteredResources(final List<Group> groups,
      final ResourceType type) {
    final List<Resource> allResources = new ArrayList<Resource>();
    for (final Group group : groups) {
      allResources.addAll(group.getResources());
    }
    // retain only resources of needed type
    final List<Resource> filteredResources = new ArrayList<Resource>();
    for (final Resource resource : allResources) {
      if (type == resource.getType()) {
        filteredResources.add(resource);
      }
    }
    return filteredResources;
  }
}
