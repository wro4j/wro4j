package ro.isdc.wro.util.concurrent;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hides the details of running tasks in parallel.
 *
 * @author Alex Objelean
 * @since 1.7.1
 * @date 26 Aug 2013
 */
public class TaskExecutor<T> {
  private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);
  /**
   * Responsible for consume the available results. The consumer is run asynchronously to avoid blocking the job
   * execution.
   */
  private ExecutorService executorService;

  private ExecutorService getExecutor() {
    if (executorService == null) {
      executorService = newExecutor();
    }
    return executorService;
  }

  /**
   * The implementation uses jsr166 ForkJoinPool implementation in case it is available and can be used, otherwise the
   * default {@link ExecutorService} is used.
   *
   * @return the {@link ExecutorService} responsible for running the tasks.
   */
  protected ExecutorService newExecutor() {
    try {
      final ExecutorService executor = (ExecutorService) Class.forName("java.util.concurrent.ForkJoinPool").newInstance();
      LOG.debug("Using ForkJoinPool as task executor.");
      return executor;
    } catch (final Exception e) {
      LOG.debug("ForkJoinPool class is not available, using default executor.", e);
      return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
  }

  /**
   * TODO rename to submitAll.
   * <p/>
   * Submits a chunk of jobs for parallel execution. This is a blocking operation - it will end execution when all
   * submitted tasks are finished.
   *
   * @param callables
   *          a {@link Collection} of {@link Callable} to execute. When the provided collection contains only one item,
   *          the task will be executed synchronously.
   * @throws Exception
   *           if an exception occurred during callable execution.
   */
  public void submit(final Collection<Callable<T>> callables)
      throws Exception {
    notNull(callables);

    final CountDownLatch latch = new CountDownLatch(callables.size());

    final long start = System.currentTimeMillis();
    final AtomicLong totalTime = new AtomicLong();
    LOG.debug("running {} tasks", callables.size());

    LOG.debug("Running tasks in parallel");
    for (final Callable<T> callable : callables) {
      getExecutor().submit(new Callable<T>() {
        public T call()
            throws Exception {
          final long begin = System.currentTimeMillis();
          try {
            return callable.call();
          } catch (final Exception e) {
            //propagate the most relevant exception
            Exception rootException = e;
            if (e instanceof ExecutionException) {
              if (e.getCause() instanceof Exception) {
                rootException = (Exception) e.getCause();
              }
            }
            LOG.error("Exception while consuming result", e);
            onException(rootException);
            return null;
          } finally {
            latch.countDown();
            final long end = System.currentTimeMillis();
            totalTime.addAndGet(end - begin);
          }
        }
      });
    }
    latch.await();
    destroy();

    LOG.debug("Number of Tasks: {}", callables.size());
    final long averageExecutionTime = callables.size() != 0 ? totalTime.longValue() / callables.size() : 0;
    LOG.debug("Average Execution Time: {}", averageExecutionTime);
    LOG.debug("Total Task Time: {}", totalTime);
    LOG.debug("Grand Total Execution Time: {}", System.currentTimeMillis() - start);
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
   * Shutdown all executors used by this class.
   */
  public void destroy() {
    if (executorService != null) {
      executorService.shutdown();
    }
  }
}
