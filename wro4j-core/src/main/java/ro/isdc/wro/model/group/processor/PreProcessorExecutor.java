package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ExceptionHandlingProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.MinimizeAwareProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.support.ProcessorsUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * TODO: refactor this class. Apply all preProcessor on provided {@link Resource} and returns the result of execution as
 * String.
 * <p>
 * This is useful when you want to preProcess a resource which is not a part of the model (css import use-case).
 * 
 * @author Alex Objelean
 */
public class PreProcessorExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(PreProcessorExecutor.class);
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  @Inject
  private ProcessorsFactory processorsFactory;
  @Inject
  private WroConfiguration config;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;  
  @Inject
  private Injector injector;
  /**
   * Runs the preProcessing in parallel.
   */
  private ExecutorService executor;
  
  /**
   * Apply preProcessors on resources and merge them.
   * 
   * @param resources
   *          what are the resources to merge.
   * @param minimize
   *          whether minimize aware processors must be applied or not.
   * @return preProcessed merged content.
   */
  public String processAndMerge(final List<Resource> resources, final boolean minimize)
      throws IOException {
    callbackRegistry.onBeforeMerge();
    try {
      Validate.notNull(resources);
      LOG.debug("process and merge resources: {}", resources);
      final StringBuffer result = new StringBuffer();
      if (shouldRunInParallel(resources)) {
        result.append(runInParallel(resources, minimize));
      } else {
        for (final Resource resource : resources) {
          LOG.debug("\tmerging resource: {}", resource);
          result.append(applyPreProcessors(resource, minimize));
        }
      }
      return result.toString();
    } finally {
      callbackRegistry.onAfterMerge();
    }
  }
  
  private boolean shouldRunInParallel(final List<Resource> resources) {
    final boolean isParallel = config.isParallelPreprocessing();
    final int availableProcessors = Runtime.getRuntime().availableProcessors();
    return isParallel && resources.size() > 1 && availableProcessors > 1;
  }
  
  /**
   * runs the pre processors in parallel.
   * 
   * @return merged and pre processed content.
   */
  private String runInParallel(final List<Resource> resources, final boolean minimize)
      throws IOException {
    LOG.debug("Running preProcessing in Parallel");
    final StringBuffer result = new StringBuffer();
    final List<Callable<String>> callables = new ArrayList<Callable<String>>();
    for (final Resource resource : resources) {
      // decorate with ContextPropagatingCallable in order to allow spawn threads to access the Context
      callables.add(new ContextPropagatingCallable<String>(new Callable<String>() {
        public String call()
            throws Exception {
          LOG.debug("Callable started for resource: {} ...", resource);
          return applyPreProcessors(resource, minimize);
        }
      }));
    }
    final ExecutorService exec = getExecutorService();
    final List<Future<String>> futures = new ArrayList<Future<String>>();
    for (final Callable<String> callable : callables) {
      futures.add(exec.submit(callable));
    }
    
    for (final Future<String> future : futures) {
      try {
        result.append(future.get());
      } catch (final Exception e) {
        // propagate original cause
        final Throwable cause = e.getCause();
        if (cause instanceof WroRuntimeException) {
          throw (WroRuntimeException) cause;
        } else if (cause instanceof IOException) {
          throw (IOException) cause;
        } else {
          throw new WroRuntimeException("Problem during parallel pre processing", e.getCause());
        }
      }
    }
    return result.toString();
  }
  
  private ExecutorService getExecutorService() {
    if (executor == null) {
      // use at most the number of available processors (true parallelism)
      final int threadPoolSize = Runtime.getRuntime().availableProcessors();
      LOG.debug("Parallel thread pool size: {}", threadPoolSize);
      executor = Executors.newFixedThreadPool(threadPoolSize,
          WroUtil.createDaemonThreadFactory("parallelPreprocessing"));
    }
    return executor;
  }
  
  /**
   * Apply a list of preprocessors on a resource.
   * 
   * @param resource
   *          the {@link Resource} on which processors will be applied
   * @param processors
   *          the list of processor to apply on the resource.
   */
  private String applyPreProcessors(final Resource resource, final boolean minimize)
      throws IOException {
    //TODO: apply filtering inside a specialized decorator
    final Collection<ResourcePreProcessor> processors = ProcessorsUtils.filterProcessorsToApply(minimize,
        resource.getType(), processorsFactory.getPreProcessors());
    LOG.debug("applying preProcessors: {}", processors);
    String resourceContent = getResourceContent(resource);
    if (processors.isEmpty()) {
      return resourceContent;
    }
    Writer writer = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourcePreProcessor processor : processors) {
      stopWatch.start("Processor: " + processor.getClass().getSimpleName());
      
      callbackRegistry.onBeforePreProcess();
      
      writer = new StringWriter();
      final Reader reader = new StringReader(resourceContent);
      try {
        //decorate and process
        decoratePreProcessor(processor).process(resource, reader, writer);
        //use the outcome for next input
        resourceContent = writer.toString();
      } finally {
        stopWatch.stop();
        callbackRegistry.onAfterPreProcess();
        reader.close();
        writer.close();
      }
    }
    LOG.debug(stopWatch.prettyPrint());
    return writer.toString();
  }
  
  /**
   * Decorates preProcessor with mandatory decorators.
   */
  private ResourcePreProcessor decoratePreProcessor(final ResourcePreProcessor processor) {
    ResourcePreProcessor decorated = new ExceptionHandlingProcessorDecorator(new MinimizeAwareProcessorDecorator(processor)); 
    injector.inject(decorated);
    return decorated;
  }
  
  /**
   * @return a Reader for the provided resource.
   * @param resource
   *          {@link Resource} which content to return.
   * @param resources
   *          the list of all resources processed in this context, used for duplicate resource detection.
   */
  private String getResourceContent(final Resource resource)
      throws IOException {
    InputStream is = null; 
    try {
      is = new BOMInputStream(uriLocatorFactory.locate(resource.getUri()));
      final String result = IOUtils.toString(is, config.getEncoding());
      if (StringUtils.isEmpty(result)) {
        LOG.debug("Empty resource detected: {}", resource.getUri());
      }
      return result;
    } catch (final IOException e) {
      LOG.debug("Invalid resource found: {}", resource);
      if (config.isIgnoreMissingResources()) {
        return StringUtils.EMPTY;
      } else {
        LOG.error("Cannot ignore missing resource:  {}", resource);
        throw e;
      }
    } finally {
      IOUtils.closeQuietly(is);
    }
  }
}