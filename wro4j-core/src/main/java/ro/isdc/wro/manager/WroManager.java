/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.exception.UnauthorizedRequestException;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.RequestUriParser;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.util.WroUtil;

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
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  private CacheStrategy<CacheEntry, String> cacheStrategy;

  /**
   * Scheduled executors service, used to update the output result.
   */
  private ScheduledExecutorService scheduler;

  /**
   * Perform processing of the uri.
   *
   * @param request {@link HttpServletRequest} to process.
   * @throws IOException.
   */
  public void process(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    InputStream is = null;
    // create model
    final WroModel model = modelFactory.getInstance();
    //Use @Inject annotation to access model inside RequestProcessors
    //TODO: move to corresponding RequestProcessor
    LOG.debug("processing: " + request.getRequestURI());
    if (request.getRequestURI().contains(CssUrlRewritingProcessor.PATH_RESOURCES)) {
      is = locateInputeStream(request);
    } else {
      is = buildGroupsInputStream(model, request, response);
    }
    OutputStream os = null;
    // append result to response stream
    if (Context.get().isGzipEnabled()) {
      os = getGzipedOutputStream(response);
    } else {
      os = response.getOutputStream();
    }
    IOUtils.copy(is, os);
    is.close();
    os.close();
  }

  /**
   * Add gzip header to response and wrap the response {@link OutputStream} with {@link GZIPOutputStream}
   *
   * @param response {@link HttpServletResponse} object.
   * @return wrapped gziped OutputStream.
   */
  private OutputStream getGzipedOutputStream(final HttpServletResponse response)
    throws IOException {
    // gzip response
    WroUtil.addGzipHeader(response);
    // Create a gzip stream
    final OutputStream os = new GZIPOutputStream(response.getOutputStream());
    LOG.debug("Gziping outputStream response");
    return os;
  }

  /**
   * @param requestURI uri of the request which encodes information about groups.
   * @param type
   * @return {@link InputStream} for groups found in requestURI.
   */
	private InputStream buildGroupsInputStream(final WroModel model, final HttpServletRequest request, final HttpServletResponse response) {
	  final String requestURI = request.getRequestURI();
    InputStream is = null;
    final ResourceType type = requestUriParser.getResourceType(requestURI);

		final StopWatch stopWatch = new StopWatch();
    stopWatch.start();
		validate();
		// find names & type
		final String groupName = requestUriParser.getGroupName(requestURI);
		if (groupName == null) {
		  throw new WroRuntimeException("No groups found for request: " + requestURI);
		}
		initScheduler(model);

		// find processed result for a group
    final Group group = model.getGroupByName(groupName);
    final List<Group> groupAsList = new ArrayList<Group>();
    groupAsList.add(group);
		String result = null;
	  final CacheEntry cacheEntry = new CacheEntry(groupName, type);
	  // Cache based on uri
	  result = cacheStrategy.get(cacheEntry);
	  if (result == null) {
	    // process groups & put update cache
	    result = groupsProcessor.process(groupAsList, type);
	    cacheStrategy.put(cacheEntry, result);
	  }
	  is = new ByteArrayInputStream(result.getBytes());
		stopWatch.stop();
		LOG.debug("WroManager process time: " + stopWatch.toString());
    if (type != null) {
      response.setContentType(type.getContentType());
    }
		return is;
	}

  /**
   * @param model
   */
  private void initScheduler(final WroModel model) {
    if (scheduler == null) {
      scheduler = Executors.newSingleThreadScheduledExecutor();
		}
    //Shutdown if any are running, just to be sure we are starting fresh new task
    scheduler.shutdown();
    final long period = Context.get().getApplicationSettings().getCacheUpdatePeriod();
    if (period > 0) {
      // Run a scheduled task which updates the model
      scheduler.scheduleAtFixedRate(getSchedulerRunnable(model), 0, period, TimeUnit.SECONDS);
    }
  }

  /**
   * @param model
   * @return
   */
  private Runnable getSchedulerRunnable(final WroModel model) {
    return new Runnable() {
    	public void run() {
    		try {
    			// process groups & put update cache
    			final Collection<Group> groups = model.getGroups();
    			// update cache for all resources
    			for (final Group group : groups) {
    				for (final ResourceType resourceType : ResourceType.values()) {
    					if (group.hasResourcesOfType(resourceType)) {
    						final Collection<Group> groupAsList = new HashSet<Group>();
    						groupAsList.add(group);
    						final String result = groupsProcessor.process(groupAsList, resourceType);
    						cacheStrategy.put(new CacheEntry(group.getName(), resourceType), result);
    					}
    				}
    			}
    		} catch (final Exception e) {
    			//Catch all exception in order to avoid situation when scheduler runs out of threads.
    			LOG.error("Exception occured: ", e);
    		}
      }
    };
  }


  /**
   * Resolve the stream for a request.
   *
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
  public final void setCacheStrategy(final CacheStrategy<CacheEntry, String> cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
  }

  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return uriLocatorFactory;
  }

  /**
   * @return the modelFactory
   */
  public WroModelFactory getModelFactory() {
    return this.modelFactory;
  }

  /**
   * Called when {@link WroManager} is being taken out of service.
   */
  public void destroy() {
    LOG.debug("WroManager destroyed");
    cacheStrategy.destroy();
    scheduler.shutdownNow();
  }
}
