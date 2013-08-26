package ro.isdc.wro.maven.plugin.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import ro.isdc.wro.util.concurrent.TaskExecutor;


/**
 * An extension of {@link TaskExecutor} which uses {@link ForkJoinPool} to parallelize tasks. This implementation is
 * more efficient than the original one.
 *
 * @author Alex Objeleana
 * @since 1.7.1
 * @date 26 Aug 2013
 */
public class ForkJoinTaskExecutor<T>
    extends TaskExecutor<T> {
  @Override
  protected ExecutorService newExecutor() {
    return new ForkJoinPool();
  }
}
