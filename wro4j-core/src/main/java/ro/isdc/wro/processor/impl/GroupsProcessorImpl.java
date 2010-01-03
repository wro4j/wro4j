/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
    final StringBuffer result = new StringBuffer();
    //using ListIterator because the collection can be changed during processing
    //be sure we use a list which supports add & remove operation on iterator while iterating
    final List<Resource> resourceList = new LinkedList<Resource>(resources);
    final ListIterator<Resource> iterator = resourceList.listIterator();
    //start with first
    iterator.next();
    for (int i = 0; i < resourceList.size(); i++) {
      final Resource resource = resourceList.get(i);
      LOG.debug("\tmerging resource: " + resource);
      String preProcessedContent = null;
      // get original content
      final Reader reader = resource.getReader();
      final String originalContent = IOUtils.toString(reader);
      reader.close();

      // preProcessing
      if (ResourceType.CSS == resource.getType()) {
        preProcessedContent = applyPreProcessors(resource,
            getCssPreProcessors(), originalContent);
      } else {
        preProcessedContent = applyPreProcessors(resource,
            getJsPreProcessors(), originalContent);
      }
      preProcessedContent = applyPreProcessors(resource,
          getAnyResourcePreProcessors(), preProcessedContent);
      result.append(preProcessedContent);
    }
    return result.toString();
  }

//  private static void test() {
//    final List<String> list = new ArrayList<String>();
//    list.add("A");
//    list.add("B");
//    list.add("C");
//    list.add("D");
//
//    final ListIterator<String> iter = list.listIterator();
//    //iter.next();
//    for (int i = 0; i < list.size(); i++) {
//      process(list.get(i), iter);
//      if (iter.hasNext()) {
//        iter.next();
//      }
//    }
//    System.out.println("list is: " + list);
//  }
//
//  private static void process(final Object o, final ListIterator iterator) {
//    if (RandomUtils.nextInt(100) < 20) {
//      //iterator.remove();
//      iterator.add("a");
//      iterator.add("b");
//      iterator.add("c");
//    } else {
//      System.out.println(o);
//    }
//  }

  /**
   * Apply resourcePreProcessors.
   *
   * @param resource
   *          {@link Resource} to pre process.
   * @param processors
   *          a list of processor to apply on the content from the supplied
   *          writer.
   * @param content
   *          The content to preProcess as String.
   * @return the processed content as String.
   */
  private String applyPreProcessors(final Resource resource,
      final List<ResourcePreProcessor> processors, final String content)
      throws IOException {
    if (content == null) {
      throw new NullPointerException("content cannot be null!");
    }
    if (processors.isEmpty()) {
      return content;
    }
    Reader input = new StringReader(content);
    Writer output = null;
    for (final ResourcePreProcessor processor : processors) {
      LOG.debug("applyinig processor:  " + processor);
      output = new StringWriter();
      processor.process(resource, input, output);
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
      LOG.debug("apply Post processor: " + processor);
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
