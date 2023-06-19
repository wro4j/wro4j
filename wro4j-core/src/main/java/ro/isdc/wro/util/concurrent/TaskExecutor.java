package ro.isdc.wro.util.concurrent;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.StopWatch;


/**
 * Hides the details of running tasks in parallel.
 *
 * @author Alex Objelean
 * @since 1.7.1
 */
public class TaskExecutor<T> {
  private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);
  /**
   * Run the tasks in parallel.
   */
  private CompletionService<T> completionService;
  /**
   * Responsible for consume the available results. The consumer is run asynchronously to avoid blocking the job
   * execution.
   */
  private ExecutorService completionExecutor;

  private CompletionService<T> getCompletionService() {
    if (completionService == null) {
      completionService = new ExecutorCompletionService<T>(getExecutor());
    }
    return completionService;
  }

  private ExecutorService getExecutor() {
    if (completionExecutor == null) {
      completionExecutor = newExecutor();
    }
    return completionExecutor;
  }

  /**
   * The implementation uses jsr166 ForkJoinPool implementation in case it is available and can be used,
   * otherwise the default {@link ExecutorService} is used.
   *
   * @return the {@link ExecutorService} responsible for running the tasks.
   */
  protected ExecutorService newExecutor() {
    try {
      final ExecutorService executor = (ExecutorService) Class.forName("java.util.concurrent.ForkJoinPool").getDeclaredConstructor().newInstance();
      LOG.debug("Using ForkJoinPool as task executor.");
      return executor;
    } catch (final Exception e) {
      LOG.debug("ForkJoinPool class is not available, using default executor.", e);
      return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
  }

  /**
   * <p>TODO rename to submitAll.</p>
   *
   * <p>Submits a chunk of jobs for parallel execution. This is a blocking operation - it will end execution when all
   * submitted tasks are finished.</p>
   * 
   * @param callables
   *          a {@link Collection} of {@link Callable} to execute. When the provided collection contains only one item,
   *          the task will be executed synchronously.
   * @throws Exception
   *           if an exception occurred during callable execution.
   */
  public void submit(final Collection<Callable<T>> callables)
      throws Exception {
    Validate.notNull(callables);

    final StopWatch watch = new StopWatch();
    watch.start("init");
    final long start = System.currentTimeMillis();
    final AtomicLong totalTime = new AtomicLong();
    LOG.debug("running {} tasks", callables.size());

    if (callables.size() == 1) {
      final T result = callables.iterator().next().call();
      onResultAvailable(result);
    } else {
      LOG.debug("Running tasks in parallel");
      watch.stop();

      watch.start("submit tasks");
      for (final Callable<T> callable : callables) {
        getCompletionService().submit(decorate(callable, totalTime));
      }
      watch.stop();

      watch.start("consume results");
      for (int i = 0; i < callables.size(); i++) {
        doConsumeResult();
      }
    }
    watch.stop();
    destroy();

    LOG.debug("Number of Tasks: {}", callables.size());
    final long averageExecutionTime = callables.size() != 0 ? totalTime.longValue() / callables.size() : 0;
    LOG.debug("Average Execution Time: {}", averageExecutionTime);
    LOG.debug("Total Task Time: {}", totalTime);
    LOG.debug("Grand Total Execution Time: {}", System.currentTimeMillis() - start);
    LOG.debug(watch.prettyPrint());
  }

  private Callable<T> decorate(final Callable<T> decorated, final AtomicLong totalTime) {
    return new Callable<T>() {
      public T call()
          throws Exception {
        final long begin = System.currentTimeMillis();
        try {
          return decorated.call();
        } finally {
          final long end = System.currentTimeMillis();
          totalTime.addAndGet(end - begin);
        }
      }
    };
  }

  /**
   * Invoked when an exception occurs during task execution. By default exception is ignored.
   *
   * @param e
   *          {@link Exception} caught during execution.
   */
  protected void onException(final Exception e)
      throws Exception {
  }

  /**
   * A callback invoked when a result is available.
   *
   * @param result
   *          the available processed result.
   * @throws Exception
   *           if callback execution fails.
   */
  protected void onResultAvailable(final T result)
      throws Exception {
  }

  private void doConsumeResult()
      throws Exception {
    try {
      final T result = getCompletionService().take().get();
      onResultAvailable(result);
    } catch (final Exception e) {
      //propagate the most relevant exception
      Exception rootException = e;
      if (e instanceof ExecutionException) {
        if (e.getCause() instanceof Exception) {
          rootException = (Exception) e.getCause();
        }
      }
      onException(rootException);
      LOG.error("Exception while consuming result", e);
    }
  }

  /**
   * Shutdown all executors used by this class.
   */
  public void destroy() {
    if (completionExecutor != null) {
      completionExecutor.shutdown();
    }
  }
}
