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

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.config.ConfigurationContext;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.http.UnauthorizedRequestException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.util.WroUtil;

/**
 * WroManager. Contains all the factories used by optimizer in order to perform the logic.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public class WroManager implements WroConfigurationChangeListener {
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
    LOG.info("processing: " + request.getRequestURI());
    validate();
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
    if (ConfigurationContext.get().getConfig().isGzipEnabled()
      && isGzipSupported()) {
      os = getGzipedOutputStream(response);
    } else {
      os = response.getOutputStream();
    }
    IOUtils.copy(is, os);
    is.close();
    os.close();
  }

  /**
   * @return true if Gzip is Supported
   */
  protected boolean isGzipSupported() {
    return WroUtil.isGzipSupported(Context.get().getRequest());
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
    final ResourceType type = groupExtractor.getResourceType(requestURI);

		final StopWatch stopWatch = new StopWatch();
    stopWatch.start();
		// find names & type
		final String groupName = groupExtractor.getGroupName(requestURI);
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
	  LOG.info("Searching cache entry: " + cacheEntry);
	  // Cache based on uri
	  result = cacheStrategy.get(cacheEntry);
	  if (result == null) {
	    LOG.info("Cache is empty. Perform processing");
	    // process groups & put result in the cache
	    result = groupsProcessor.process(groupAsList, type);
	    cacheStrategy.put(cacheEntry, result);
	  }
	  is = new ByteArrayInputStream(result.getBytes());
		stopWatch.stop();
		LOG.info("WroManager process time: " + stopWatch.toString());
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
      final long period = ConfigurationContext.get().getConfig().getCacheUpdatePeriod();
      LOG.info("runing thread with period of " + period);
      if (period > 0) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Run a scheduled task which updates the model
        scheduler.scheduleAtFixedRate(getSchedulerRunnable(model), 0, period, TimeUnit.SECONDS);
      }
    }
  }


  /**
   * @param model Model containing
   * @return a {@link Runnable} which will update the cache content with latest data.
   */
  private Runnable getSchedulerRunnable(final WroModel model) {
    return new Runnable() {
    	public void run() {
    		try {
    		  LOG.info("reloading cache");
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
    final UriLocator uriLocator = groupsProcessor.getUriLocatorFactory().getInstance(resourceId);
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
   * {@inheritDoc}
   */
  public void onCachePeriodChanged() {
    LOG.info("onCachePeriodChanged");
    if (scheduler != null) {
      scheduler.shutdown();
      scheduler = null;
    }
    //flush the cache by destroying it.
    cacheStrategy.clear();
  }

  /**
   * {@inheritDoc}
   */
  public void onModelPeriodChanged() {
    LOG.info("onModelPeriodChanged");
    //update the cache also when model is changed.
    onCachePeriodChanged();
  	if (modelFactory instanceof WroConfigurationChangeListener) {
  		((WroConfigurationChangeListener)modelFactory).onModelPeriodChanged();
  	}
  }

  /**
   * Called when {@link WroManager} is being taken out of service.
   */
  public void destroy() {
    LOG.debug("WroManager destroyed");
    cacheStrategy.destroy();
    modelFactory.destroy();
    if (scheduler != null) {
      scheduler.shutdownNow();
    }
  }

	/**
	 * Check if all dependencies are set.
	 */
	private void validate() {
		if (this.groupExtractor == null) {
			throw new WroRuntimeException("GroupExtractor was not set!");
		}
		if (this.modelFactory == null) {
			throw new WroRuntimeException("ModelFactory was not set!");
		}
		if (this.groupsProcessor == null) {
			throw new WroRuntimeException("GroupsProcessor was not set!");
		}
		if (this.cacheStrategy == null) {
			throw new WroRuntimeException("cacheStrategy was not set!");
		}
	}

  /**
   * @param groupExtractor the uriProcessor to set
   */
  public final void setGroupExtractor(final GroupExtractor groupExtractor) {
    if (groupExtractor == null) {
      throw new IllegalArgumentException("GroupExtractor cannot be null!");
    }
    this.groupExtractor = groupExtractor;
  }

  /**
   * @param groupsProcessor the groupsProcessor to set
   */
  public final void setGroupsProcessor(final GroupsProcessor groupsProcessor) {
    if (groupsProcessor == null) {
      throw new IllegalArgumentException("GroupsProcessor cannot be null!");
    }
    this.groupsProcessor = groupsProcessor;
  }

  /**
   * @param modelFactory the modelFactory to set
   */
  public final void setModelFactory(final WroModelFactory modelFactory) {
    if (modelFactory == null) {
      throw new IllegalArgumentException("WroModelFactory cannot be null!");
    }
    this.modelFactory = modelFactory;
  }

  /**
   * @param cacheStrategy the cache to set
   */
  public final void setCacheStrategy(final CacheStrategy<CacheEntry, String> cacheStrategy) {
    if (cacheStrategy == null) {
      throw new IllegalArgumentException("cacheStrategy cannot be null!");
    }
    this.cacheStrategy = cacheStrategy;
  }

  /**
   * @return the modelFactory
   */
  public WroModelFactory getModelFactory() {
    return this.modelFactory;
  }
}
