package ro.isdc.wro.maven.plugin.support;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.concurrent.TaskExecutor;


/**
 * An extension of {@link TaskExecutor} which uses {@link ForkJoinPool} to parallelize tasks if the jsr166 is available
 * (when jdk7 is used or appropriate jvm args are provided with jsr166 dependency). This implementation is more
 * efficient than the original one.
 *
 * @author Alex Objeleana
 * @since 1.7.1
 * @date 26 Aug 2013
 */
public class ForkJoinTaskExecutor<T>
    extends TaskExecutor<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ForkJoinTaskExecutor.class);

  /**
   * The implementation uses jsr166 {@link ForkJoinPool} implementation in case it is available and can be used,
   * otherwise the default {@link ExecutorService} is used.
   */
  @Override
  protected ExecutorService newExecutor() {
    try {
      return (ExecutorService) Class.forName("java.util.concurrent.ForkJoinPool").newInstance();
    } catch (final Exception e) {
      LOG.debug("ForkJoinPool class is not available, using default executor. ", e);
      return super.newExecutor();
    }
  }
}
