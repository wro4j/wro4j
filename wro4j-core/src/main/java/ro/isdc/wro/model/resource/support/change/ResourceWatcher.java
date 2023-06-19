package ro.isdc.wro.model.resource.support.change;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.http.handler.ResourceWatcherRequestHandler;
import ro.isdc.wro.http.support.PreserveDetailsRequestWrapper;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.support.DispatcherStreamLocator;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.AbstractCssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.util.DestroyableLazyInitializer;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * A runnable responsible for watching if any resources were changed and invalidate the cache entry for the group
 * containing obsolete resources. This class is thread-safe.
 *
 * @author Alex Objelean
 * @since 1.4.8
 */
public class ResourceWatcher
    implements Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceWatcher.class);
  /**
   * The thread pool size of the executor which is responsible for performing async check
   */
  private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

  public static interface Callback {
    /**
     * Callback method invoked when a group change is detected.
     *
     * @param key
     *          {@link CacheKey} associated with the group whose change was detected.
     */
    void onGroupChanged(final CacheKey key);

    /**
     * Invoked when the change of the resource is detected.
     *
     * @param resource
     *          the {@link Resource} which changed.
     */
    void onResourceChanged(final Resource resource);
  }

  /**
   * Default implementation of {@link Callback} which does nothing by default.
   */
  public static class CallbackSupport
      implements Callback {
	@Override
    public void onGroupChanged(final CacheKey key) {
    }

	@Override
    public void onResourceChanged(final Resource resource) {
    };
  }

  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private Injector injector;
  @Inject
  private LifecycleCallbackRegistry lifecycleCallback;
  @Inject
  private ResourceChangeDetector resourceChangeDetector;
  @Inject
  private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
  @Inject
  private ReadOnlyContext context;
  @Inject
  private DispatcherStreamLocator dispatcherLocator;
  /**
   * Executor responsible for running the check asynchronously.
   */
  private final DestroyableLazyInitializer<ExecutorService> executorServiceRef = new DestroyableLazyInitializer<ExecutorService>() {
    @Override
    protected ExecutorService initialize() {
      return Executors.newFixedThreadPool(POOL_SIZE, WroUtil.createDaemonThreadFactory(ResourceWatcher.class.getName()));
    }

    @Override
    public void destroy() {
      if (isInitialized()) {
        get().shutdownNow();
      }
      super.destroy();
    };
  };

  /**
   * Default constructor with a NoOP callback.
   */
  public void check(final CacheKey cacheKey) {
    check(cacheKey, new CallbackSupport());
  }

  /**
   * Check if resources from a group were changed. If a change is detected, the changeListener will be invoked.
   *
   * @param cacheKey
   *          the cache key which was requested. The key contains the groupName which has to be checked for changes.
   */
  public void check(final CacheKey cacheKey, final Callback callback) {
	Validate.notNull(cacheKey);
    LOG.debug("started");
    final StopWatch watch = new StopWatch();
    watch.start("detect changes");
    try {
      final Group group = new WroModelInspector(modelFactory.create()).getGroupByName(cacheKey.getGroupName());
      if (isGroupChanged(group.collectResourcesOfType(cacheKey.getType()), callback)) {
        callback.onGroupChanged(cacheKey);
        cacheStrategy.put(cacheKey, null);
      }
      resourceChangeDetector.reset();
    } catch (final Exception e) {
      onException(e);
    } finally {
      watch.stop();
      LOG.debug("resource watcher info: {}", watch.prettyPrint());
    }
  }

  /**
   * <p>Will try an asynchronous check if the async configuration is enabled. If async check is not configured, a
   * synchronous check will be performed. The async check assumes that the {@link ResourceWatcherRequestHandler} is
   * enabled.</p>
   *
   * <p>If the async check is not allowed (the request was not passed through {@link WroFilter}) - no check will be
   * performed. This is important for use-cases when wro resource is included using a taglib which performs a wro api
   * call directly, without being invoked through {@link WroFilter}.</p>
   *
   * @return true if the actual check invocation was performed. This is important to decide if the resource change
   *         should be skipped or not.
   */
  public boolean tryAsyncCheck(final CacheKey cacheKey) {
    boolean checkInvoked = false;
    if (context.getConfig().isResourceWatcherAsync()) {
      if (isAsyncCheckAllowed()) {
        LOG.debug("Checking resourceWatcher asynchronously...");
        final Callable<Void> callable = createAsyncCheckCallable(cacheKey);
        submit(callable);
        checkInvoked = true;
      }
    } else {
      LOG.debug("Async check not allowed. Falling back to sync check.");
      check(cacheKey);
      checkInvoked = true;
    }
    return checkInvoked;
  }

  /**
   * @return true only if the async is enabled by configuration and if the original request was for a wro resource
   *         (passed through {@link WroFilter}).
   */
  private boolean isAsyncCheckAllowed() {
    return WroFilter.isPassedThroughyWroFilter(Context.get().getRequest());
  }

  /**
   * @VisibleForTesting
   * @param callable
   *          {@link Callable} to submit for asynchronous execution.
   */
  void submit(final Callable<Void> callable) {
    executorServiceRef.get().submit(callable);
  }

  /**
   * Invoked when exception occurs.
   */
  protected void onException(final Exception e) {
    // not using ERROR log intentionally, since this error is not that important
    LOG.info("Could not check for resource changes because: {}", e.getMessage());
    LOG.debug("[FAIL] detecting resource change ", e);
  }

  private boolean isGroupChanged(final Group group, final Callback callback) {
    final List<Resource> resources = group.getResources();
    final AtomicBoolean isChanged = new AtomicBoolean(false);
    final List<Future<?>> futures = new ArrayList<Future<?>>();
    final boolean isAsync = context.getConfig().isResourceWatcherAsync();
    try {
      for (final Resource resource : resources) {
        if (isAsync) {
          futures.add(executorServiceRef.get().submit(ContextPropagatingCallable.decorate(new Callable<Void>() {
        	@Override
            public Void call()
                throws Exception {
              checkResourceChange(resource, group, callback, isChanged);
              return null;
            }
          })));
        } else {
          checkResourceChange(resource, group, callback, isChanged);
        }
      }
      if (isAsync) {
        LOG.debug("await async execution");
        //await for all futures to complete before returning the result
        for (final Future<?> future : futures) {
          future.get();
        }
      }
    } catch (final Exception e) {
      LOG.debug("Exception while onResourceChange is invoked", e);
    }
    LOG.debug("group={}, changed={}", group.getName(), isChanged);
    return isChanged.get();
  }

  /**
   * Will check if a given resource was changed and will invoke the appropriate callback.
   */
  private void checkResourceChange(final Resource resource, final Group group, final Callback callback,
      final AtomicBoolean isChanged)
      throws Exception {
    if (isChanged(resource, group.getName())) {
      isChanged.compareAndSet(false, true);
      callback.onResourceChanged(resource);
      lifecycleCallback.onResourceChanged(resource);
    }
  }

  /**
   * Check if the resource was changed from previous run. The implementation uses resource content digest (hash) to
   * check for change.
   *
   * @param resource
   *          the {@link Resource} to check.
   * @return true if the resource was changed.
   */
  private boolean isChanged(final Resource resource, final String groupName) {
    boolean changed = false;
    try {
      final String uri = resource.getUri();
      // using AtomicBoolean because we need to mutate this variable inside an anonymous class.
      final AtomicBoolean changeDetected = new AtomicBoolean(resourceChangeDetector.checkChangeForGroup(uri, groupName));
      if (!changeDetected.get() && resource.getType() == ResourceType.CSS) {
        final Reader reader = new InputStreamReader(locatorFactory.locate(uri), Charset.defaultCharset());
        LOG.debug("\tCheck @import directive from {}", resource);
        createCssImportProcessor(changeDetected, groupName).process(resource, reader, new StringWriter());
      }
      changed = changeDetected.get();
    } catch (final IOException e) {
      LOG.debug("[FAIL] Cannot check {} resource (Exception message: {}). Assuming it is unchanged...", resource,
          e.getMessage());
    }
    LOG.debug("resource={}, changed={}", resource.getUri(), changed);
    return changed;
  }

  /**
   * @param changeDetected
   *          - flag indicating if the change is detected. When this value is true, the processing will be interrupted
   *          by throwing a {@link RuntimeException}.
   * @param groupName
   *          the name of the group being processed.
   * @return a processor used to detect changes in imported resources.
   */
  private ResourcePreProcessor createCssImportProcessor(final AtomicBoolean changeDetected, final String groupName) {
    final ResourcePreProcessor cssImportProcessor = new AbstractCssImportPreProcessor() {
      @Override
      protected void onImportDetected(final String importedUri) {
        LOG.debug("Found @import {}", importedUri);
        final boolean isImportChanged = isChanged(Resource.create(importedUri, ResourceType.CSS), groupName);
        LOG.debug("\tisImportChanged={}", isImportChanged);
        if (isImportChanged) {
          changeDetected.set(true);
          // we need to continue in order to store the hash for all imported resources, otherwise the change won't be
          // computed correctly.
        }
      };

      @Override
      protected String doTransform(final String cssContent, final List<Resource> foundImports)
          throws IOException {
        // no need to build the content, since we are interested in finding imported resources only
        return "";
      }

      @Override
      public String toString() {
        return CssImportPreProcessor.class.getSimpleName();
      }
    };
    /**
     * Ignore processor failure, since we are interesting in detecting change only. A failure is treated as lack of
     * change.
     */
    final ResourcePreProcessor processor = new ExceptionHandlingProcessorDecorator(cssImportProcessor) {
      @Override
      protected boolean isIgnoreFailingProcessor() {
        return true;
      }
    };
    injector.inject(processor);
    return processor;
  }

  private Callable<Void> createAsyncCheckCallable(final CacheKey cacheKey) {
    final HttpServletRequest originalRequest = Context.get().getRequest();
    LOG.debug("OriginalRequest: url={}, uri={}, servletPath={}", originalRequest.getRequestURL(),
        originalRequest.getRequestURI(), originalRequest.getServletPath());
    final HttpServletRequest request = new PreserveDetailsRequestWrapper(originalRequest);
    return ContextPropagatingCallable.decorate(new Callable<Void>() {
      @Override
      public Void call()
          throws Exception {
        final String location = ResourceWatcherRequestHandler.createHandlerRequestPath(cacheKey, request);
        try {
          dispatcherLocator.locateExternal(request, location);
          return null;
        } catch (final IOException e) {
          final StringBuilder message = new StringBuilder("Could not check the following cacheKey: ").append(cacheKey);
          if (e instanceof SocketTimeoutException) {
            message.append(". The invocation of ").append(location).append(
                " timed out. Consider increasing the connectionTimeout configuration.");
            LOG.error(message.toString());
          } else {
            LOG.error(message.toString(), e);
          }
          throw e;
        }
      }
    });
  }

  /**
   * @VisibleForTesting
   */
  ResourceChangeDetector getResourceChangeDetector() {
    return resourceChangeDetector;
  }

  @Override
  public void destroy()
      throws Exception {
    executorServiceRef.destroy();
  }
}
