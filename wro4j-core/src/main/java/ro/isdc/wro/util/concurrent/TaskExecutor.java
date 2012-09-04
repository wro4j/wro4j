package ro.isdc.wro.util.concurrent;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
  private ExecutorService completionExecutor;
  /**
   * Responsible for consume the available results. The consumer is run asynchronously to avoid blocking the job
   * execution.
   */
  private ExecutorService consumerService;
  
  private CompletionService<T> getCompletionService() {
    if (completionService == null) {
      completionService = new ExecutorCompletionService<T>(getExecutor());
    }
    return completionService;
  }
  
  public ExecutorService getExecutor() {
    if (completionExecutor == null) {
      completionExecutor = newExecutor();
    }
    return completionExecutor;
  }
  
  /**
   * @return a not null instance of {@link ExecutorService} which is created lazily.
   */
  private ExecutorService getConsumerService() {
    if (consumerService == null) {
      // it is enough to use an executor with a single thread.
      consumerService = Executors.newFixedThreadPool(1);
    }
    return consumerService;
  }
  
  /**
   * @return the {@link ExecutorService} responsible for running the tasks.
   */
  protected ExecutorService newExecutor() {
    return Executors.newFixedThreadPool(5);
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
    LOG.debug("running {} tasks", callables.size());
    if (callables.size() == 1) {
      final T result = callables.iterator().next().call();
      onResultAvailable(result);
    } else {
      LOG.debug("Running tasks in parallel");
      for (final Callable<T> callable : callables) {
        getCompletionService().submit(callable);
      }
      for (int i = 0; i < callables.size(); i++) {
        doConsumeResult();
      }
      //need to find out how to safely shutdown the executor
      //getExecutor().shutdown();
    }
  }
  
  /**
   * Submits a task to the taskExecutor. This operation is not a blocking one.
   */
  private final void submit(final Callable<T> callable) {
    getCompletionService().submit(callable);
    consumeResult();
  }
  
  /**
   * Reads asynchronously the latest available result.
   */
  private void consumeResult() {
    getConsumerService().submit(new Callable<Void>() {
      public Void call()
          throws Exception {
        doConsumeResult();
        return null;
      }
    });
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
  
  private static void printDate() {
    System.out.println(new SimpleDateFormat("HH:ss:S").format(new Date()));
  }
  
  public static void main(final String[] args)
      throws Exception {
    final TaskExecutor<String> executor = new TaskExecutor<String>() {
      @Override
      protected void onResultAvailable(final String result)
          throws Exception {
        System.out.println("result available: " + result);
        System.out.println("\n===============\n");
        printDate();
      }
      
    };
    executor.submit(new Callable<String>() {
      public String call()
          throws Exception {
        printDate();
        String result = Thread.currentThread().getName();
        System.out.println("\n[START]===============\nThread " + result + " started...");
        double sleep = 2000;
        System.out.println("Sleeping for: " + sleep);
        Thread.sleep((long) sleep);
        return result;
      }
    });
    for (int i = 0; i < 10; i++) {
      executor.submit(new Callable<String>() {
        public String call()
            throws Exception {
          printDate();
          String result = Thread.currentThread().getName() + ":" + UUID.randomUUID().toString();
          System.out.println("\n[START]===============\nThread " + result + " started...");
          double sleep = Math.random() * 300;
          System.out.println("Sleeping for: " + sleep);
          Thread.sleep((long) sleep);
          return result;
        }
      });
    }
  }
}
