/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
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
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public final class GroupsProcessorImpl extends AbstractGroupsProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(GroupsProcessorImpl.class);

  private static class DefaultPreProcessorExecutor implements PreProcessorExecutor {
    AbstractGroupsProcessor groupsProcessor;
    public DefaultPreProcessorExecutor(final AbstractGroupsProcessor groupsProcessor) {
      this.groupsProcessor = groupsProcessor;
    }

    /**
     * {@inheritDoc}
     */
    public String execute(final Resource resource) throws IOException {
      if (resource == null) {
        throw new IllegalArgumentException("Resource cannot be null!");
      }
      //merge preProcessorsBy type and anyPreProcessors
      final Collection<ResourcePreProcessor> processors = groupsProcessor.getPreProcessorsByType(resource.getType());
      processors.addAll(groupsProcessor.getPreProcessorsByType(null));
      final Writer output = applyPreProcessors(processors, resource);
      return output.toString();
    }


    /**
     * Apply a list of preprocessors on a resource.
     */
    private Writer applyPreProcessors(final Collection<ResourcePreProcessor> processors, final Resource resource)
      throws IOException {
      // get original content
      Reader reader = null;
      String content = "";
      try {
        reader = groupsProcessor.getResourceReader(resource);
        content = IOUtils.toString(reader);
        reader.close();
      } catch (final IOException e) {
        LOG.warn("Invalid resource found" + resource);
      }
      if (processors.isEmpty() || reader == null) {
        final Writer output = new StringWriter();
        output.write(content);
        return output;
      }
      Reader input = new StringReader(content);
      Writer output = null;
      for (final ResourcePreProcessor processor : processors) {
        output = new StringWriter();
        processor.process(resource, input, output);
        input = new StringReader(output.toString());
      }
      return output;
    }
  }

  private PreProcessorExecutor preProcessorExecutor = new DefaultPreProcessorExecutor(this);

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean acceptAnnotatedField(final Object processor, final Field field)
    throws IllegalAccessException {
    boolean accept = super.acceptAnnotatedField(processor, field);
    if (field.getType().equals(PreProcessorExecutor.class)) {
      field.set(processor, preProcessorExecutor);
      LOG.debug("Successfully injected field: " + field.getName());
      accept = true;
    }
    return accept;
  }

  /**
   * {@inheritDoc} While processing the resources, if any exception occurs - it
   * is wrapped in a RuntimeException.
   */
  public String process(final Collection<Group> groups, final ResourceType type) {
    if (groups == null) {
      throw new IllegalArgumentException("List of groups cannot be null!");
    }
    if (type == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    final List<Resource> filteredResources = getFilteredResources(groups, type);
    try {
      // Merge
      String result = preProcessAndMerge(filteredResources);
      // postProcessing
      result = applyPostProcessors(type, result);
      return result;
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources", e);
    }
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
    final StringBuffer result = new StringBuffer();
    for (final Resource resource : resources) {
      LOG.debug("\tmerging resource: " + resource);
      result.append(preProcessorExecutor.execute(resource));
    }
    return result.toString();
  }

  //TODO use generified version
  private String applyPostProcessors(final ResourceType resourceType, final String content)
    throws IOException {
    if (content == null) {
      throw new IllegalArgumentException("content cannot be null!");
    }
    final Collection<ResourcePostProcessor> typeProcessors = getPostProcessorsByType(resourceType);
    String output = applyPostProcessors(typeProcessors, content);
    final Collection<ResourcePostProcessor> anyProcessors = getPostProcessorsByType(null);
    output = applyPostProcessors(anyProcessors, output.toString());
    return output.toString();
  }

  /**
   * TODO use generics & combine this & above methods
   * <p>
   * Apply resourcePostProcessors.
   *
   * @param processors a collection of processors to apply on the content from the supplied writer.
   * @param content to process with all postProcessors.
   * @return Writer the processed content is written to this writer.
   */
  private String applyPostProcessors(final Collection<ResourcePostProcessor> processors, final String content)
    throws IOException {
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content.toString());
    Writer output = null;
    for (final ResourcePostProcessor processor : processors) {
      LOG.debug("apply Post processor: " + processor);
      output = new StringWriter();
      processor.process(input, output);
      input = new StringReader(output.toString());
    }
    return output.toString();
  }
}
