package ro.isdc.wro.config.support;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.support.FieldsSavingRequestWrapper;
import ro.isdc.wro.manager.runnable.ResourceWatcherRunnable;


/**
 * Responsible for inherit the {@link Context} from the current thread and use it in child thread. Similar to
 * {@link ContextPropagatingCallable}, but the {@link Context} is inherited instead of refering the same instance. This
 * is useful when you need to create a thread which needs to have access to {@link Context} even outside of the request
 * cycle (ex: {@link ResourceWatcherRunnable}).
 * 
 * @author Alex Objelean
 * @created 8 Aug 2012
 * @since 1.4.8
 */
public class InheritableContextRunnable
    implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(InheritableContextRunnable.class);
  private final Runnable decorated;
  private final Context inheritedContext;
  
  public InheritableContextRunnable(final Runnable decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
    final Context context = Context.get();
    inheritedContext = Context.webContext(new FieldsSavingRequestWrapper(context.getRequest()), context.getResponse(),
        context.getFilterConfig());
  }
  
  /**
   * {@inheritDoc}
   */
  public void run() {
    try {
      Context.set(inheritedContext);
      decorated.run();
      // TODO find a way to destroy the context
    } catch (Exception e) {
      LOG.error("Exception while running decorated " + InheritableContextRunnable.class.getName(), e);
    }
  }
}
