package ro.isdc.wro.model.resource.support.change;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
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
import ro.isdc.wro.util.io.NullOutputStream;


/**
 * A runnable responsible for watching if any resources were changed and invalidate the cache entry for the group
 * containing obsolete resources. This class is thread-safe.
 * 
 * @author Alex Objelean
 * @created 06 Aug 2012
 * @since 1.4.8
 */
public class ResourceWatcher
    implements Destroyable {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceWatcher.class);
  /**
   * The thread pool size of the executor which is responsible for performing async check
   */
  private static final int POOL_SIZE = 2;
  
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
    public void onGroupChanged(final CacheKey key) {
    }
    
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
   * Invokes asynchronously the check by invoking the handler. This is required, to achieve safe asynchronous behavior.
   */
  public void checkAsync(final CacheKey cacheKey) {
    if (context.getConfig().isResourceWatcherAsync()) {
      LOG.debug("Checking resourceWatcher asynchronously...");
      final Callable<Void> callable = createAsyncCheckCallable(cacheKey);
      submit(callable);
    } else {
      check(cacheKey);
    }
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
   * Check if resources from a group were changed. If a change is detected, the changeListener will be invoked.
   * 
   * @param cacheKey
   *          the cache key which was requested. The key contains the groupName which has to be checked for changes.
   */
  public void check(final CacheKey cacheKey, final Callback callback) {
    notNull(cacheKey);
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
   * Invoked when exception occurs.
   */
  protected void onException(final Exception e) {
    // not using ERROR log intentionally, since this error is not that important
    LOG.info("Could not check for resource changes because: {}", e.getMessage());
    LOG.debug("[FAIL] detecting resource change ", e);
  }
  
  private boolean isGroupChanged(final Group group, final Callback callback) {
    // TODO run the check in parallel?
    final List<Resource> resources = group.getResources();
    boolean isChanged = false;
    for (final Resource resource : resources) {
      if (isChanged |= isChanged(resource, group.getName())) {
        try {
          callback.onResourceChanged(resource);
          lifecycleCallback.onResourceChanged(resource);
        } catch (final Exception e) {
          LOG.debug("Exception while onResourceChange is invoked", e);
        }
      }
    }
    LOG.debug("group={}, changed={}", group.getName(), isChanged);
    return isChanged;
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
        final Reader reader = new InputStreamReader(locatorFactory.locate(uri));
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
    final HttpServletRequest request = new PreserveDetailsRequestWrapper(Context.get().getRequest());
    final HttpServletResponse response = wrapResponse(Context.get().getResponse());
    return new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        final String location = ResourceWatcherRequestHandler.createHandlerRequestPath(cacheKey, request, response);
        try {
          dispatcherLocator.getInputStream(request, response, location);
          return null;
        } catch (final IOException e) {
          final StringBuffer message = new StringBuffer("Could not check the following cacheKey: " + cacheKey);
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
   * TODO create a callback to check for error and log it when needed.
   */
  private HttpServletResponse wrapResponse(final HttpServletResponse response) {
    return new HttpServletResponseWrapper(response) {
      private final PrintWriter printWriter = new PrintWriter(new NullOutputStream());
      
      @Override
      public PrintWriter getWriter()
          throws IOException {
        return printWriter;
      }
    };
  }
  
  /**
   * @VisibleForTesting
   */
  ResourceChangeDetector getResourceChangeDetector() {
    return resourceChangeDetector;
  }
  
  public void destroy()
      throws Exception {
    executorServiceRef.destroy();
  }
}
