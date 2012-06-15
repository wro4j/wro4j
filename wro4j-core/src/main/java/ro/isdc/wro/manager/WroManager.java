/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.WroConfigurationChangeListener;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.http.support.UnauthorizedRequestException;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.util.HashBuilder;
import ro.isdc.wro.model.resource.util.NamingStrategy;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.SchedulerHelper;
import ro.isdc.wro.util.Transformer;
import ro.isdc.wro.util.WroUtil;


/**
 * Contains all the factories used by optimizer in order to perform the logic. This object should be created through
 * {@link WroManagerFactory}, in order to ensure that all dependencies are injected properly. In other words, avoid
 * setting the fields explicitly after creating a new instance of {@link WroManager}
 * 
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public class WroManager
  implements WroConfigurationChangeListener {
  private static final Logger LOG = LoggerFactory.getLogger(WroManager.class);
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private GroupExtractor groupExtractor;
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  @Inject
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  @Inject
  private ProcessorsFactory processorsFactory;
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  /**
   * Rename the file name based on its original name and content.
   */
  @Inject
  private NamingStrategy namingStrategy;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private GroupsProcessor groupsProcessor;
  @Inject
  private WroConfiguration config;
  /**
   * HashBuilder for creating a hash based on the processed content.
   */
  @Inject
  private HashBuilder hashBuilder;
  /**
   * A list of model transformers. Allows manager to mutate the model before it is being parsed and processed.
   */
  private List<Transformer<WroModel>> modelTransformers = Collections.emptyList();
  /**
   * Schedules the cache update.
   */
  private final SchedulerHelper cacheSchedulerHelper;
  /**
   * Schedules the model update.
   */
  private final SchedulerHelper modelSchedulerHelper;
  
  public WroManager() {
    cacheSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new ReloadCacheRunnable(WroManager.this);
      }
    }, ReloadCacheRunnable.class.getSimpleName());
    modelSchedulerHelper = SchedulerHelper.create(new LazyInitializer<Runnable>() {
      @Override
      protected Runnable initialize() {
        return new ReloadModelRunnable(WroManager.this);
      }
    }, ReloadModelRunnable.class.getSimpleName());
  }


  /**
   * Perform processing of the uri.
   *
   * @throws IOException when any IO related problem occurs or if the request cannot be processed.
   */
  public final void process()
    throws IOException {
    validate();
    if (isProxyResourceRequest()) {
      serveProxyResourceRequest();
    } else {
      serveProcessedBundle();
    }
  }


  /**
   * Check if this is a request for a proxy resource - a resource which url is overwritten by wro4j.
   */
  private boolean isProxyResourceRequest() {
    final HttpServletRequest request = Context.get().getRequest();
    return request != null && StringUtils.contains(request.getRequestURI(), CssUrlRewritingProcessor.PATH_RESOURCES);
  }


  private boolean isGzipAllowed() {
    return config.isGzipEnabled() && isGzipSupported();
  }


  /**
   * Write to stream the content of the processed resource bundle.
   *
   * @param model the model used to build stream.
   */
  private void serveProcessedBundle()
    throws IOException {
    final Context context = Context.get();
    final WroConfiguration configuration = context.getConfig();

    final HttpServletRequest request = context.getRequest();
    final HttpServletResponse response = context.getResponse();

    OutputStream os = null;
    try {
      // find names & type
      final ResourceType type = groupExtractor.getResourceType(request);
      final String groupName = groupExtractor.getGroupName(request);
      final boolean minimize = groupExtractor.isMinimized(request);
      if (groupName == null || type == null) {
        throw new WroRuntimeException("No groups found for request: " + request.getRequestURI());
      }
      initAggregatedFolderPath(request, type);

      // reschedule cache & model updates
      cacheSchedulerHelper.scheduleWithPeriod(configuration.getCacheUpdatePeriod());
      modelSchedulerHelper.scheduleWithPeriod(configuration.getModelUpdatePeriod());

      final CacheEntry cacheKey = new CacheEntry(groupName, type, minimize);
      final ContentHashEntry cacheValue = cacheStrategy.get(cacheKey);

      // TODO move ETag check in wroManagerFactory
      final String ifNoneMatch = request.getHeader(HttpHeader.IF_NONE_MATCH.toString());
      
      // enclose etag value in quotes to be compliant with the RFC
      final String etagValue = String.format("\"%s\"", cacheValue.getHash());

      if (etagValue != null && etagValue.equals(ifNoneMatch)) {
        LOG.debug("ETag hash detected: {}. Sending {} status code", etagValue, HttpServletResponse.SC_NOT_MODIFIED);
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        // because we cannot return null, return a stream containing nothing.
        // TODO close output stream?
        return;
      }
      /**
       * Set contentType before actual content is written, solves <br/>
       * <a href="http://code.google.com/p/wro4j/issues/detail?id=341">issue341</a>
       */
      if (type != null) {
        response.setContentType(type.getContentType() + "; charset=" + configuration.getEncoding());
      }
      // set ETag header
      response.setHeader(HttpHeader.ETAG.toString(), etagValue);

      os = response.getOutputStream();
      if (cacheValue.getRawContent() != null) {
        // use gziped response if supported & Set content length based on gzip flag
        if (isGzipAllowed()) {
          response.setContentLength(cacheValue.getGzippedContent().length);
          // add gzip header and gzip response
          response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
          response.setHeader("Vary", "Accept-Encoding");
          IOUtils.write(cacheValue.getGzippedContent(), os);
        } else {
          response.setContentLength(cacheValue.getRawContent().length());
          IOUtils.write(cacheValue.getRawContent(), os, configuration.getEncoding());
        }
      }
    } finally {
      if(os != null)
        IOUtils.closeQuietly(os);
    }
  }


  /**
   * Set the aggregatedFolderPath if required.
   */
  private void initAggregatedFolderPath(final HttpServletRequest request, final ResourceType type) {
    if (ResourceType.CSS == type && Context.get().getAggregatedFolderPath() == null) {
      final String requestUri = request.getRequestURI();
      final String cssFolder = StringUtils.removeEnd(requestUri, FilenameUtils.getName(requestUri));
      final String aggregatedFolder = StringUtils.removeStart(cssFolder, request.getContextPath());
      Context.get().setAggregatedFolderPath(aggregatedFolder);
    }
  }


  /**
   * Encodes a fingerprint of the resource into the path. The result may look like this: ${fingerprint}/myGroup.js
   *
   * @return a path to the resource with the fingerprint encoded as a folder name.
   */
  public final String encodeVersionIntoGroupPath(final String groupName, final ResourceType resourceType,
    final boolean minimize) {
    final CacheEntry key = new CacheEntry(groupName, resourceType, minimize);
    final ContentHashEntry cacheValue = cacheStrategy.get(key);
    final String groupUrl = groupExtractor.encodeGroupUrl(groupName, resourceType, minimize);
    // encode the fingerprint of the resource into the resource path
    return formatVersionedResource(cacheValue.getHash(), groupUrl);
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
   * Serve images and other external resources referred by bundled resources.
   *
   * @param request {@link HttpServletRequest} object.
   * @param outputStream where the stream will be written.
   * @throws IOException if no stream could be resolved.
   */
  private void serveProxyResourceRequest()
    throws IOException {
    final HttpServletRequest request = Context.get().getRequest();
    final OutputStream outputStream = Context.get().getResponse().getOutputStream();

    final String resourceId = request.getParameter(CssUrlRewritingProcessor.PARAM_RESOURCE_ID);
    LOG.debug("locating stream for resourceId: {}", resourceId);
    final CssUrlRewritingProcessor processor = ProcessorsUtils.findPreProcessorByClass(CssUrlRewritingProcessor.class,
      processorsFactory.getPreProcessors());
    if (processor != null && (!config.isDebug() && !processor.isUriAllowed(resourceId))) {
      throw new UnauthorizedRequestException("Unauthorized resource request detected! " + request.getRequestURI());
    }
    final InputStream is = uriLocatorFactory.locate(resourceId);
    if (is == null) {
      throw new WroRuntimeException("Cannot process request with uri: " + request.getRequestURI());
    }
    IOUtils.copy(is, outputStream);
    IOUtils.closeQuietly(is);
    IOUtils.closeQuietly(outputStream);
  }


  /**
   * {@inheritDoc}
   */
  public final void onCachePeriodChanged(final long period) {
    LOG.info("onCachePeriodChanged with value {} has been triggered!", period);
    cacheSchedulerHelper.scheduleWithPeriod(period);
    // flush the cache by destroying it.
    cacheStrategy.clear();
  }


  /**
   * {@inheritDoc}
   */
  public final void onModelPeriodChanged(final long period) {
    LOG.info("onModelPeriodChanged with value {} has been triggered!", period);
    //trigger model destroy
    getModelFactory().destroy();
    modelSchedulerHelper.scheduleWithPeriod(period);
  }


  /**
   * Called when {@link WroManager} is being taken out of service.
   */
  public final void destroy() {
    try {
      cacheSchedulerHelper.destroy();
      modelSchedulerHelper.destroy();
      cacheStrategy.destroy();
      modelFactory.destroy();
    } catch (final Exception e) {
      LOG.error("Exception occured during manager destroy!!!");
    } finally {
      LOG.info("WroManager destroyed");
    }
  }


  /**
   * Check if all dependencies are set.
   */
  private void validate() {
    Validate.notNull(cacheStrategy, "cacheStrategy was not set!");
    Validate.notNull(groupsProcessor, "groupsProcessor was not set!");
    Validate.notNull(uriLocatorFactory, "uriLocatorFactory was not set!");
    Validate.notNull(processorsFactory, "processorsFactory was not set!");
    Validate.notNull(groupExtractor, "GroupExtractor was not set!");
    Validate.notNull(modelFactory, "ModelFactory was not set!");
    Validate.notNull(cacheStrategy, "cacheStrategy was not set!");
    Validate.notNull(hashBuilder, "HashBuilder was not set!");
  }

  /**
   * @return true if Gzip is Supported
   */
  private boolean isGzipSupported() {
    return WroUtil.isGzipSupported(Context.get().getRequest());
  }


  /**
   * @param groupExtractor the uriProcessor to set
   */
  public final WroManager setGroupExtractor(final GroupExtractor groupExtractor) {
    Validate.notNull(groupExtractor);
    this.groupExtractor = groupExtractor;
    return this;
  }

  public final WroManager setModelFactory(final WroModelFactory modelFactory) {
    Validate.notNull(modelFactory);
    this.modelFactory = modelFactory;
    return this;
  }


  /**
   * @param cacheStrategy the cache to set
   */
  public final WroManager setCacheStrategy(final CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy) {
    Validate.notNull(cacheStrategy);
    this.cacheStrategy = cacheStrategy;
    return this;
  }


  /**
   * @param contentDigester the contentDigester to set
   */
  public final WroManager setHashBuilder(final HashBuilder contentDigester) {
    Validate.notNull(contentDigester);
    this.hashBuilder = contentDigester;
    return this;
  }

  
  public final HashBuilder getHashBuilder() {
    return hashBuilder;
  }


  /**
   * @return the modelFactory
   */
  public final WroModelFactory getModelFactory() {
    return modelFactory;
  }


  /**
   * @return the processorsFactory used by this WroManager.
   */
  public final ProcessorsFactory getProcessorsFactory() {
    return processorsFactory;
  }


  /**
   * @param processorsFactory the processorsFactory to set
   */
  public final WroManager setProcessorsFactory(final ProcessorsFactory processorsFactory) {
    this.processorsFactory = processorsFactory;
    return this;
  }


  public final void setNamingStrategy(final NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }


  /**
   * @param uriLocatorFactory the uriLocatorFactory to set
   */
  public final WroManager setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
    return this;
  }


  /**
   * @return the cacheStrategy
   */
  public final CacheStrategy<CacheEntry, ContentHashEntry> getCacheStrategy() {
    return cacheStrategy;
  }


  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return uriLocatorFactory;
  }

  /**
   * @return The strategy used to rename bundled resources.
   */
  public final NamingStrategy getNamingStrategy() {
    return this.namingStrategy;
  }
  

  public final GroupExtractor getGroupExtractor() {
    return groupExtractor;
  }

  public final GroupsProcessor getGroupsProcessor() {
    return this.groupsProcessor;
  }

  /**
   * Registers a callback.
   *
   * @param callback {@link LifecycleCallback} to register.
   */
  public final void registerCallback(final LifecycleCallback callback) {
    Validate.notNull(callback);
    callbackRegistry.registerCallback(callback);
  }
  

  public final List<Transformer<WroModel>> getModelTransformers() {
    return modelTransformers;
  }


  public final void setModelTransformers(final List<Transformer<WroModel>> modelTransformers) {
    this.modelTransformers = modelTransformers;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}