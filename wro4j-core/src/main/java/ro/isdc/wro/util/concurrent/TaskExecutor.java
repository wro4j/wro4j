package ro.isdc.wro.util.concurrent;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
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
   * @return the {@link ExecutorService} responsible for running the tasks.
   */
  protected ExecutorService newExecutor() {
    return Executors.newFixedThreadPool(3);
  }

  /**
   * @param callables
   *          a {@link Collection} of {@link Callable} to execute.
   * @throws Exception
   *           if an exception occured during callable execution.
   */
  public final void submit(final Collection<Callable<T>> callables)
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
    LOG.debug("Average Execution Time: {}", totalTime.longValue() / callables.size());
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
      onException(e);
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
