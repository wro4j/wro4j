/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support;

import org.apache.commons.lang3.Validate;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import ro.isdc.wro.util.ObjectFactory;


/**
 * @author Alex Objelean
 * @created 10 Nov 2011
 * @since 1.4.2
 */
public class RhinoEnginePool<T> {
  private static final int MAX_ACTIVE = 5;
  private static final int MAX_IDLE = 1;
  private static final byte EXHAUSTED_ACTION = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
  private static final long MAX_WAIT = 1000L * 5L;
  // Allows using the engine in a thread-safe fashion.
  private GenericObjectPool enginePool;


  public RhinoEnginePool(final ObjectFactory<T> engineFactory) {
    Validate.notNull(engineFactory);
    enginePool = new GenericObjectPool(new BasePoolableObjectFactory() {
      @Override
      public Object makeObject()
        throws Exception {
        return engineFactory.create();
      }
    }, MAX_ACTIVE, EXHAUSTED_ACTION, MAX_WAIT, MAX_IDLE);
  }


  @SuppressWarnings("unchecked")
  public T getEngine() {
    try {
      return (T) enginePool.borrowObject();
    } catch (final Exception e) {
      // should never happen
      throw new RuntimeException("Cannot get object from the pool", e);
    }
  }


  public void returnEngine(final T engine) {
    Validate.notNull(engine);
    try {
      enginePool.returnObject(engine);
    } catch (final Exception e) {
      // should never happen
      throw new RuntimeException("Cannot get object from the pool", e);
    }
  }
}
