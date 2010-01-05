/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.processor.GroupExtractor;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * WroManager. Contains all the factories used by optimizer in order to perform the logic.
 *
 * @author Alex Objelean
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
   * GroupExtractor.
   */
  private GroupExtractor groupExtractor;

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
   * Perform processing of the uri and depending on it will return {@link WroProcessResult} object containing
   * the input stream of the requested resource and it's content type.
   *
   * @param uri to process.
   * @return {@link WroProcessResult} object.
   */
  //TODO pass request instead of uri
  public WroProcessResult process(final String uri) {
    validate();
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    // find names & type
    final List<String> groupNames = groupExtractor.getGroupNames(uri);
    final ResourceType type = groupExtractor.getResourceType(uri);

    // create model & find groups
    final WroModel model = modelFactory.getInstance();
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
   * TODO move to process method
   * Resolve the stream for a request.
   * @param request {@link HttpServletRequest} object.
   * @return {@link InputStream} not null object if the resource is valid and can be accessed
   * @throws WroRuntimeException if no stream could be resolved.
   */
  public InputStream getStreamForRequest(final HttpServletRequest request) throws IOException {
    final String resourceId = request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
    final UriLocator uriLocator = getUriLocatorFactory().getInstance(resourceId);
    final CssUrlRewritingProcessor processor = groupsProcessor.findPreProcessorByClass(CssUrlRewritingProcessor.class);
    if (processor != null && !processor.isUriAllowed(resourceId)) {
      throw new WroRuntimeException("Unauthorized resource request detected!");
    }
    final InputStream is = uriLocator.locate(resourceId);
    if (is == null) {
      throw new WroRuntimeException("Could not Locate resource: " + resourceId);
    }
    return is;
  }

  /**
   * Check if all dependencies are set.
   */
  private void validate() {
    try {
      if (this.groupExtractor == null) {
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
  public final void setGroupExtractor(final GroupExtractor uriProcessor) {
    this.groupExtractor = uriProcessor;
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
