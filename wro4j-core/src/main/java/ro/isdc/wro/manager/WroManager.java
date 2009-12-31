/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;


import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.UriProcessor;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * WroManager. Contains all the factories used by optimizer in order to perform the logic.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public final class WroManager {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WroManager.class);

  /**
   * ResourcesModel factory.
   */
  private WroModelFactory modelFactory;

  /**
   * UriProcessor.
   */
  private UriProcessor uriProcessor;

  /**
   * UriLocatorFactory.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Groups processor.
   */
  private GroupsProcessor groupsProcessor;

  /**
   * A cacheStrategy used for caching processed results. Key: uri, Value: processed result.
   */
  private CacheStrategy<String, String> cacheStrategy;

  /**
   * Default constructor.
   */
  public WroManager() {
  }

  /**
   * Perform processing of the uri and depending on it will return {@link WroProcessResult} object containing
   * the input stream of the requested resource and it's content type.
   *
   * @param uri
   * @return
   */
  //TODO pass request instead of uri
  public WroProcessResult process(final String uri) {
    validate();
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    // find names & type
    final List<String> groupNames = uriProcessor.getGroupNames(uri);
    final ResourceType type = uriProcessor.getResourceType(uri);

    // create model & find groups
    final WroModel model = modelFactory.getInstance(uriLocatorFactory);
    final List<Group> groups = model.getGroupsByNames(groupNames);

    String processedResult = null;
    if (!Context.get().isDevelopmentMode()) {
      // Cache based on uri
      processedResult = cacheStrategy.get(uri);
      if (processedResult == null) {
        // process groups & put update cache
        processedResult = groupsProcessor.process(groups, type);
        cacheStrategy.put(uri, processedResult);
      }
    } else {
      processedResult = groupsProcessor.process(groups, type);
    }
    final WroProcessResult result = new WroProcessResult();
    result.setResourceType(type);
    result.setInputStream(new ByteArrayInputStream(processedResult.getBytes()));

    stopWatch.stop();
    LOG.info("WroManager process time: " + stopWatch.toString());
    return result;
  }

  /**
   * Check if all dependencies are set.
   */
  private void validate() {
    try {
      if (this.uriProcessor == null) {
        throw new IllegalStateException("UriProcessor was not set!");
      }
      if (this.modelFactory == null) {
        throw new IllegalStateException("ModelFactory was not set!");
      }
      if (this.groupsProcessor == null) {
        throw new IllegalStateException("GroupProcessor was not set!");
      }
      if (this.uriLocatorFactory == null) {
        throw new IllegalStateException("uriLocatorFactory was not set!");
      }
      if (this.cacheStrategy == null) {
        throw new IllegalStateException("cacheStrategy was not set!");
      }
    } catch (final Exception e) {
      throw new WroRuntimeException(e);
    }
  }

  /**
   * @param uriProcessor the uriProcessor to set
   */
  public final void setUriProcessor(final UriProcessor uriProcessor) {
    this.uriProcessor = uriProcessor;
  }

  /**
   * @param groupsProcessor the groupsProcessor to set
   */
  public final void setGroupsProcessor(final GroupsProcessor groupsProcessor) {
    this.groupsProcessor = groupsProcessor;
  }

  /**
   * @param modelFactory the modelFactory to set
   */
  public final void setModelFactory(final WroModelFactory modelFactory) {
    this.modelFactory = modelFactory;
  }

  /**
   * @param uriLocatorFactory the resourceLocatorFactory to set
   */
  public final void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
  }

  /**
   * @param cacheStrategy the cache to set
   */
  public final void setCacheStrategy(final CacheStrategy<String, String> cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
  }

  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return uriLocatorFactory;
  }
}
