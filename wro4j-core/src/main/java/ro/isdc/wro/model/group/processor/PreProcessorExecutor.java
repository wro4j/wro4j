package ro.isdc.wro.model.group.processor;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.DefaultProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.support.ProcessingCriteria;
import ro.isdc.wro.model.resource.processor.support.ProcessingType;
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
  private ReadOnlyContext context;
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private Injector injector;
  /**
   * Runs the preProcessing in parallel.
   */
  private ExecutorService executor;
  
  /**
   * Apply preProcessors on resources and merge them after all preProcessors are applied.
   * 
   * @param resources
   *          what are the resources to merge.
   * @param minimize
   *          whether minimize aware processors must be applied or not.
   * @return preProcessed merged content.
   */
  public String processAndMerge(final List<Resource> resources, final boolean minimize)
      throws IOException {
    return processAndMerge(resources, ProcessingCriteria.create(ProcessingType.ALL, minimize));
  }
  
  /**
   * Apply preProcessors on resources and merge them.
   * 
   * @param resources
   *          what are the resources to merge.
   * @param criteria
   *          {@link ProcessingCriteria} used to identify the processors to apply and those to skip.
   * @return preProcessed merged content.
   */
  public String processAndMerge(final List<Resource> resources, final ProcessingCriteria criteria)
      throws IOException {
    notNull(criteria);
    LOG.debug("criteria: {}", criteria);
    callbackRegistry.onBeforeMerge();
    if (!context.getConfig().isMinifyResources()) {
      LOG.debug("Minification disabled through mbean");
      criteria.setMinimize(false);
    }
    try {
      Validate.notNull(resources);
      LOG.debug("process and merge resources: {}", resources);
      final StringBuffer result = new StringBuffer();
      if (shouldRunInParallel(resources)) {
        result.append(runInParallel(resources, criteria));
      } else {
        for (final Resource resource : resources) {
          LOG.debug("\tmerging resource: {}", resource);
          result.append(applyPreProcessors(resource, criteria));
        }
      }
      return result.toString();
    } finally {
      callbackRegistry.onAfterMerge();
    }
  }
  
  private boolean shouldRunInParallel(final List<Resource> resources) {
    final boolean isParallel = context.getConfig().isParallelPreprocessing();
    final int availableProcessors = Runtime.getRuntime().availableProcessors();
    return isParallel && resources.size() > 1 && availableProcessors > 1;
  }
  
  /**
   * runs the pre processors in parallel.
   * 
   * @return merged and pre processed content.
   */
  private String runInParallel(final List<Resource> resources, final ProcessingCriteria criteria)
      throws IOException {
    LOG.debug("Running preProcessing in Parallel");
    final StringBuffer result = new StringBuffer();
    final List<Callable<String>> callables = new ArrayList<Callable<String>>();
    for (final Resource resource : resources) {
      callables.add(new Callable<String>() {
        public String call()
            throws Exception {
          LOG.debug("Callable started for resource: {} ...", resource);
          return applyPreProcessors(resource, criteria);
        }
      });
    }
    final ExecutorService exec = getExecutorService();
    final List<Future<String>> futures = new ArrayList<Future<String>>();
    for (final Callable<String> callable : callables) {
      // decorate with ContextPropagatingCallable in order to allow spawn threads to access the Context
      final Callable<String> decoratedCallable = new ContextPropagatingCallable<String>(callable);
      futures.add(exec.submit(decoratedCallable));
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
          throw new WroRuntimeException("Problem during parallel pre processing", e);
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
  private String applyPreProcessors(final Resource resource, final ProcessingCriteria criteria)
      throws IOException {
    final Collection<ResourcePreProcessor> processors = processorsFactory.getPreProcessors();
    LOG.debug("applying preProcessors: {}", processors);
    
    String resourceContent = null;
    try {
      resourceContent = getResourceContent(resource);
    } catch (final IOException e) {
      LOG.debug("Invalid resource found: {}", resource);
      if (Context.get().getConfig().isIgnoreMissingResources()) {
        return StringUtils.EMPTY;
      } else {
        LOG.error("Cannot ignore missing resource:  {}", resource);
        throw e;
      }
    }
    if (processors.isEmpty()) {
      return resourceContent;
    }
    Writer writer = null;
    for (final ResourcePreProcessor processor : processors) {
      final ResourcePreProcessor decoratedProcessor = decoratePreProcessor(processor, criteria);
      
      writer = new StringWriter();
      final Reader reader = new StringReader(resourceContent);
      // decorate and process
      decoratedProcessor.process(resource, reader, writer);
      // use the outcome for next input
      resourceContent = writer.toString();
    }
    return writer.toString();
  }
  
  /**
   * Decorates preProcessor with mandatory decorators.
   */
  private ResourcePreProcessor decoratePreProcessor(final ResourcePreProcessor processor,
      final ProcessingCriteria criteria) {
    final ResourcePreProcessor decorated = new DefaultProcessorDecorator(processor, criteria) {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        try {
          callbackRegistry.onBeforePreProcess();
          super.process(resource, reader, writer);
        } finally {
          callbackRegistry.onAfterPreProcess();
        }
      }
    };
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
      final String result = IOUtils.toString(is, context.getConfig().getEncoding());
      if (StringUtils.isEmpty(result)) {
        LOG.debug("Empty resource detected: {}", resource.getUri());
      }
      return result;
    } finally {
      IOUtils.closeQuietly(is);
    }
  }
}
