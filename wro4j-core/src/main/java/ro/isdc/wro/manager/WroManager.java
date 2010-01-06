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
import ro.isdc.wro.exception.UnauthorizedRequestException;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.RequestUriParser;
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
  private RequestUriParser requestUriParser;

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
   * @param request {@link HttpServletRequest} to process.
   * @return {@link WroProcessResult} object.
   * @throws IOException.
   */
  public WroProcessResult process(final HttpServletRequest request) throws IOException {
    final String requestURI = request.getRequestURI();
    InputStream is = null;
    final ResourceType type = requestUriParser.getResourceType(requestURI);
    if (requestURI.contains(CssUrlRewritingProcessor.PATH_RESOURCES)) {
      is = locateInputeStream(request);
    } else {
      is = buildGroupsInputStream(requestURI, type);
    }
    final WroProcessResult result = new WroProcessResult();
    result.setResourceType(type);
    result.setInputStream(is);
    return result;
  }

  /**
   * @param requestURI uri of the request which encodes information about groups.
   * @param type
   * @return {@link InputStream} for groups found in requestURI.
   */
	private InputStream buildGroupsInputStream(final String requestURI, final ResourceType type) {
		InputStream is = null;
		final StopWatch stopWatch = new StopWatch();
		validate();
		stopWatch.start();
		// find names & type
		final List<String> groupNames = requestUriParser.getGroupNames(requestURI);
		if (groupNames.isEmpty()) {
		  throw new WroRuntimeException("No groups found for request: " + requestURI);
		}
		// create model & find groups
		final WroModel model = modelFactory.getInstance();
		final List<Group> groups = model.getGroupsByNames(groupNames);

		String processedResult = null;
		if (!Context.get().isDevelopmentMode()) {
		  // Cache based on uri
		  processedResult = cacheStrategy.get(requestURI);
		  if (processedResult == null) {
		    // process groups & put update cache
		    processedResult = groupsProcessor.process(groups, type);
		    cacheStrategy.put(requestURI, processedResult);
		  }
		} else {
		  processedResult = groupsProcessor.process(groups, type);
		}
		is = new ByteArrayInputStream(processedResult.getBytes());
		stopWatch.stop();
		LOG.debug("WroManager process time: " + stopWatch.toString());
		return is;
	}

  /**
   * Resolve the stream for a request.
   * @param request {@link HttpServletRequest} object.
   * @return {@link InputStream} not null object if the resource is valid and can be accessed
   * @throws WroRuntimeException if no stream could be resolved.
   */
  private InputStream locateInputeStream(final HttpServletRequest request) throws IOException {
    final String resourceId = request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
    LOG.debug("locating stream for resourceId: " + resourceId);
    final UriLocator uriLocator = getUriLocatorFactory().getInstance(resourceId);
    final CssUrlRewritingProcessor processor = groupsProcessor.findPreProcessorByClass(CssUrlRewritingProcessor.class);
    if (processor != null && !processor.isUriAllowed(resourceId)) {
      throw new UnauthorizedRequestException("Unauthorized resource request detected! " + request.getRequestURI());
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
		if (this.requestUriParser == null) {
			throw new WroRuntimeException("UriProcessor was not set!");
		}
		if (this.modelFactory == null) {
			throw new WroRuntimeException("ModelFactory was not set!");
		}
		if (this.groupsProcessor == null) {
			throw new WroRuntimeException("GroupProcessor was not set!");
		}
		if (this.uriLocatorFactory == null) {
			throw new WroRuntimeException("uriLocatorFactory was not set!");
		}
		if (this.cacheStrategy == null) {
			throw new WroRuntimeException("cacheStrategy was not set!");
		}
	}

  /**
   * @param requestUriParser the uriProcessor to set
   */
  public final void setRequestUriParser(final RequestUriParser requestUriParser) {
    this.requestUriParser = requestUriParser;
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
