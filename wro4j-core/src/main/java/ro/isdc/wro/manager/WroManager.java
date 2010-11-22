/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.beans.PropertyChangeListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.http.HttpHeader;
import ro.isdc.wro.http.UnauthorizedRequestException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.util.HashBuilder;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * Contains all the factories used by optimizer in order to perform the logic.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public class WroManager
  implements WroConfigurationChangeListener, CacheChangeCallbackAware {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WroManager.class);
  /**
   * wro API mapping path. If request uri contains this, exposed API method will be invoked.
   */
  public static final String PATH_API = "wroAPI";
  /**
   * API - reload cache method call
   */
  public static final String API_RELOAD_CACHE = PATH_API + "/reloadCache";
  /**
   * API - reload model method call
   */
  public static final String API_RELOAD_MODEL = PATH_API + "/reloadModel";
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
   * HashBuilder for creating a hash based on the processed content.
   */
  private HashBuilder hashBuilder;
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  /**
   * A callback to be notified about the cache change.
   */
  private PropertyChangeListener cacheChangeCallback;
  /**
   * Scheduled executors service, used to update the output result.
   */
  private ScheduledExecutorService scheduler;
  private final Injector injector;
  @Inject
  private ProcessorsFactory processorsFactory;
  @Inject
  private UriLocatorFactory uriLocatorFactory;


  public WroManager() {
    injector = new Injector(newUriLocatorFactory(), newProcessorsFactory());
    injector.inject(this);
  }

  /**
   * @return the injector
   */
  public Injector getInjector() {
    return injector;
  }


  /**
   * Override to provide a different or modified factory.
   *
   * @return {@link ProcessorsFactory} object.
   */
  protected ProcessorsFactory newProcessorsFactory() {
    return new SimpleProcessorsFactory();
  }


  /**
   * Override to provide a different or modified factory.
   *
   * @return {@link UriLocatorFactory} object.
   */
  protected UriLocatorFactory newUriLocatorFactory() {
    return new SimpleUriLocatorFactory();
  }


  /**
   * Perform processing of the uri.
   *
   * @param request {@link HttpServletRequest} to process.
   * @param response HttpServletResponse where to write the result content.
   * @throws IOException when any IO related problem occurs or if the request cannot be processed.
   */
  public final void process()
    throws IOException {
    final HttpServletRequest request = Context.get().getRequest();
    final HttpServletResponse response = Context.get().getResponse();

    LOG.debug("processing: " + request.getRequestURI());
    validate();
    InputStream is = null;
    // create model
    // TODO move API related checks into separate class and determine filter mapping for better mapping
    if (matchesUrl(request, API_RELOAD_CACHE)) {
      Context.get().getConfig().reloadCache();
      preventCacheResponse(response);
      return;
    }
    if (matchesUrl(request, API_RELOAD_MODEL)) {
      Context.get().getConfig().reloadModel();
      preventCacheResponse(response);
      return;
    }
    if (isProxyResourceRequest(request)) {
      is = locateInputeStream(request);
    } else {
      is = buildGroupsInputStream(request, response);
    }
    if (is == null) {
      throw new WroRuntimeException("Cannot process this request: " + request.getRequestURL());
    }
    // use gziped response if supported
    final OutputStream os = getGzipedOutputStream(response);
    IOUtils.copy(is, os);
    is.close();
    os.close();
  }


  /**
   * Prevent the response to be cached by browser by adding some header attributes.
   *
   * @param response {@link HttpServletResponse} to prevent cache.
   */
  private void preventCacheResponse(final HttpServletResponse response) {
    response.setHeader(HttpHeader.PRAGMA.toString(), "no-cache");
    response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "no-cache");
    response.setDateHeader(HttpHeader.EXPIRES.toString(), 0);
  }


  /**
   * Check if the request path matches the provided api path.
   */
  private boolean matchesUrl(final HttpServletRequest request, final String apiPath) {
    final Pattern pattern = Pattern.compile(".*" + apiPath + "[/]?", Pattern.CASE_INSENSITIVE);
    final Matcher m = pattern.matcher(request.getRequestURI());
    return m.matches();
  }


  /**
   * Check if this is a request for a proxy resource - a resource which url is overwritten by wro4j.
   */
  private boolean isProxyResourceRequest(final HttpServletRequest request) {
    return request.getRequestURI().contains(CssUrlRewritingProcessor.PATH_RESOURCES);
  }

  /**
   * Add gzip header to response and wrap the response {@link OutputStream} with {@link GZIPOutputStream}.
   *
   * @param response {@link HttpServletResponse} object.
   * @return wrapped gziped OutputStream.
   * @throws IOException when Gzip operation fails.
   */
  private OutputStream getGzipedOutputStream(final HttpServletResponse response)
    throws IOException {
    if (Context.get().getConfig().isGzipEnabled() && isGzipSupported()) {
      // add gzip header and gzip response
      response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
      // Create a gzip stream
      return new GZIPOutputStream(response.getOutputStream());
    }
    LOG.debug("Gziping outputStream response");
    return response.getOutputStream();
  }


  /**
   * @param model the model used to build stream.
   * @param request {@link HttpServletRequest} for this request cycle.
   * @param response {@link HttpServletResponse} used to set content type.
   * @return {@link InputStream} for groups found in requestURI or null if the request is not as expected.
   */
  private InputStream buildGroupsInputStream(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start("buildGroupsStream");
    InputStream inputStream = null;
    // find names & type
    final ResourceType type = groupExtractor.getResourceType(request);
    final String groupName = groupExtractor.getGroupName(request);
    final boolean minimize = groupExtractor.isMinimized(request);
    if (groupName == null || type == null) {
      throw new WroRuntimeException("No groups found for request: " + request.getRequestURI());
    }
    initScheduler();

    final ContentHashEntry contentHashEntry = getContentHashEntry(groupName, type, minimize);

    // TODO move ETag check in wroManagerFactory
    final String ifNoneMatch = request.getHeader(HttpHeader.IF_NONE_MATCH.toString());
    final String etagValue = contentHashEntry.getHash();
    if (etagValue != null && etagValue.equals(ifNoneMatch)) {
      LOG.debug("ETag hash detected: " + etagValue + ". Sending " + HttpServletResponse.SC_NOT_MODIFIED
        + " status code");
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      // because we cannot return null, return a stream containing nothing.
      return new ByteArrayInputStream(new byte[] {});
    }

    if (contentHashEntry.getContent() != null) {
      // make the input stream encoding aware.
      inputStream = new ByteArrayInputStream(contentHashEntry.getContent().getBytes());
    }
    if (type != null) {
      // TODO add also the charset?
      response.setContentType(type.getContentType());
    }
    // set ETag header
    response.setHeader(HttpHeader.ETAG.toString(), contentHashEntry.getHash());

    stopWatch.stop();
    LOG.debug("WroManager process time: " + stopWatch.toString());
    return inputStream;
  }


  /**
   * Encodes a fingerprint of the resource into the path. The result may look like this: ${fingerprint}/myGroup.js
   *
   * @return a path to the resource with the fingerprint encoded as a folder name.
   */
  public final String encodeVersionIntoGroupPath(final String groupName, final ResourceType resourceType,
    final boolean minimize) {
    try {
      final ContentHashEntry contentHashEntry = getContentHashEntry(groupName, resourceType, minimize);
      final String groupUrl = groupExtractor.encodeGroupUrl(groupName, resourceType, minimize);
      // encode the fingerprint of the resource into the resource path
      return formatVersionedResource(contentHashEntry.getHash(), groupUrl);
    } catch (final IOException e) {
      return "";
    }
  }


  /**
   * Format the version of the resource in the path. Default implementation use hash as a folder: <hash>/groupName.js.
   * The implementation can be changed to follow a different versioning style, like version parameter:
   * /groupName.js?version=<hash>
   *
   * @param hash Hash of the resource.
   * @param resourcePath Path of the resource.
   * @return formatted versioned path of the resource.
   */
  protected String formatVersionedResource(final String hash, final String resourcePath) {
    return String.format("%s/%s", hash, resourcePath);
  }


  /**
   * @return {@link ContentHashEntry} object.
   */
  private ContentHashEntry getContentHashEntry(final String groupName, final ResourceType type, final boolean minimize)
    throws IOException {
    final CacheEntry cacheEntry = new CacheEntry(groupName, type, minimize);
    LOG.debug("Searching cache entry: " + cacheEntry);
    // Cache based on uri
    ContentHashEntry contentHashEntry = cacheStrategy.get(cacheEntry);
    if (contentHashEntry == null) {
      LOG.debug("Cache is empty. Perform processing...");
      // process groups & put result in the cache
      // find processed result for a group
      final List<Group> groupAsList = new ArrayList<Group>();
      final Group group = modelFactory.getInstance().getGroupByName(groupName);
      groupAsList.add(group);

      final String content = getGroupsProcessor().process(groupAsList, type, minimize);
      contentHashEntry = getContentHashEntryByContent(content);
      cacheStrategy.put(cacheEntry, contentHashEntry);
    }
    return contentHashEntry;
  }


  /**
   * Creates a {@link ContentHashEntry} based on provided content.
   */
  private ContentHashEntry getContentHashEntryByContent(final String content)
    throws IOException {
    String hash = null;
    if (content != null) {
      LOG.debug("Content to fingerprint: [" + StringUtils.abbreviate(content, 40) + "]");
      hash = hashBuilder.getHash(new ByteArrayInputStream(content.getBytes()));
    }
    final ContentHashEntry entry = ContentHashEntry.valueOf(content, hash);
    LOG.debug("computed entry: " + entry);
    return entry;
  }


  /**
   * Initialize the scheduler based on configuration values.
   */
  private void initScheduler() {
    if (scheduler == null) {
      final long period = Context.get().getConfig().getCacheUpdatePeriod();
      LOG.debug("runing thread with period of " + period);
      if (period > 0) {
        scheduler = Executors.newSingleThreadScheduledExecutor(WroUtil.createDaemonThreadFactory());
        // Run a scheduled task which updates the model.
        // Here a scheduleWithFixedDelay is used instead of scheduleAtFixedRate because the later can cause a problem
        // (thread tries to make up for lost time in some situations)
        scheduler.scheduleWithFixedDelay(getSchedulerRunnable(), 0, period, TimeUnit.SECONDS);
      }
    }
  }


  /**
   * @return a {@link Runnable} which will update the cache content with latest data.
   */
  private Runnable getSchedulerRunnable() {
    return new Runnable() {
      public void run() {
        try {
          if (cacheChangeCallback != null) {
            // invoke cacheChangeCallback
            cacheChangeCallback.propertyChange(null);
          }
          LOG.info("reloading cache");
          final WroModel model = modelFactory.getInstance();
          // process groups & put update cache
          final Collection<Group> groups = model.getGroups();
          // update cache for all resources
          for (final Group group : groups) {
            for (final ResourceType resourceType : ResourceType.values()) {
              if (group.hasResourcesOfType(resourceType)) {
                final Collection<Group> groupAsList = new HashSet<Group>();
                groupAsList.add(group);
                // TODO notify the filter about the change - expose a callback
                // TODO check if request parameter can be fetched here without errors.
                // groupExtractor.isMinimized(Context.get().getRequest())
                final Boolean[] minimizeValues = new Boolean[] { true, false };
                for (final boolean minimize : minimizeValues) {
                  final String content = getGroupsProcessor().process(groupAsList, resourceType, minimize);
                  cacheStrategy.put(new CacheEntry(group.getName(), resourceType, minimize),
                    getContentHashEntryByContent(content));
                }
              }
            }
          }
        } catch (final Exception e) {
          // Catch all exception in order to avoid situation when scheduler runs out of threads.
          LOG.error("Exception occured: ", e);
        }
      }
    };
  }

  private GroupsProcessor getGroupsProcessor() {
    if (groupsProcessor == null) {
      groupsProcessor = new GroupsProcessor();
      injector.inject(groupsProcessor);
    }
    return groupsProcessor;
  }

  /**
   * {@inheritDoc}
   */
  public final void registerCallback(final PropertyChangeListener callback) {
    this.cacheChangeCallback = callback;
  }


  /**
   * Allow subclasses to turn off gzipping.
   *
   * @return true if Gzip is Supported
   */
  protected boolean isGzipSupported() {
    return WroUtil.isGzipSupported(Context.get().getRequest());
  }


  /**
   * Resolve the stream for a request.
   *
   * @param request {@link HttpServletRequest} object.
   * @return {@link InputStream} not null object if the resource is valid and can be accessed
   * @throws IOException if no stream could be resolved.
   */
  private InputStream locateInputeStream(final HttpServletRequest request)
    throws IOException {
    final String resourceId = request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
    LOG.debug("locating stream for resourceId: " + resourceId);
    final CssUrlRewritingProcessor processor = findCssUrlRewritingPreProcessor();
    if (processor != null && !processor.isUriAllowed(resourceId)) {
      throw new UnauthorizedRequestException("Unauthorized resource request detected! " + request.getRequestURI());
    }
    return uriLocatorFactory.locate(resourceId);
  }


  /**
   * @return {@link CssUrlRewritingProcessor} instance if it is used.
   */
  protected CssUrlRewritingProcessor findCssUrlRewritingPreProcessor() {
    final CssUrlRewritingProcessor processor = ProcessorsUtils.findPreProcessorByClass(CssUrlRewritingProcessor.class,
      processorsFactory.getPreProcessors());
    return processor;
  }


  /**
   * {@inheritDoc}
   */
  public final void onCachePeriodChanged() {
    LOG.info("CacheChange event triggered!");
    if (scheduler != null) {
      scheduler.shutdown();
      scheduler = null;
    }
    // flush the cache by destroying it.
    cacheStrategy.clear();
  }


  /**
   * {@inheritDoc}
   */
  public final void onModelPeriodChanged() {
    LOG.info("ModelChange event triggered!");
    // update the cache also when model is changed.
    onCachePeriodChanged();
    if (modelFactory instanceof WroConfigurationChangeListener) {
      ((WroConfigurationChangeListener)modelFactory).onModelPeriodChanged();
    }
  }


  /**
   * Called when {@link WroManager} is being taken out of service.
   */
  public final void destroy() {
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
    if (this.cacheStrategy == null) {
      throw new WroRuntimeException("cacheStrategy was not set!");
    }
    if (this.hashBuilder == null) {
      throw new WroRuntimeException("hashBuilder was not set!");
    }
  }


  /**
   * @param groupExtractor the uriProcessor to set
   */
  public final void setGroupExtractor(final GroupExtractor groupExtractor) {
    this.groupExtractor = groupExtractor;
  }


  /**
   * @param modelFactory the modelFactory to set
   */
  public final void setModelFactory(final WroModelFactory modelFactory) {
    this.modelFactory = modelFactory;
  }


  /**
   * @param cacheStrategy the cache to set
   */
  public final void setCacheStrategy(final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
  }


  /**
   * @param contentDigester the contentDigester to set
   */
  public void setHashBuilder(final HashBuilder contentDigester) {
    this.hashBuilder = contentDigester;
  }


  /**
   * @return the modelFactory
   */
  public final WroModel getModel() {
    return this.modelFactory.getInstance();
  }
}
