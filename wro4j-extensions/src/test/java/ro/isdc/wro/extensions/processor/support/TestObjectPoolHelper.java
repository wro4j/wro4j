/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.util.ObjectFactory;


/**
 * @author Alex Objelean
 */
public class TestObjectPoolHelper {
  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullArgument()
      throws Exception {
    new ObjectPoolHelper<Void>(null);
  }

  @Test(expected = NullPointerException.class)
  public void cannotReturnNullObjectToPool()
      throws Exception {
    final ObjectPoolHelper<Integer> pool = new ObjectPoolHelper<Integer>(new ObjectFactory<Integer>() {
      @Override
      public Integer create() {
        return null;
      }
    });
    final Integer object = pool.getObject();
    pool.returnObject(object);
  }

  @Test
  public void shouldReuseExistingObject()
      throws Exception {
    final ObjectPoolHelper<Integer> pool = new ObjectPoolHelper<Integer>(new ObjectFactory<Integer>() {
      @Override
      public Integer create() {
        return 3;
      }
    });
    final Integer object = pool.getObject();
    Assert.assertEquals(Integer.valueOf(3), object);
    pool.returnObject(object);
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullObjectPool()
      throws Exception {
    final ObjectPoolHelper<Integer> pool = new ObjectPoolHelper<Integer>(new ObjectFactory<Integer>() {
      @Override
      public Integer create() {
        return 3;
      }
    });
    pool.setObjectPool(null);
  }

  @Test
  public void shouldUseCustomObjectPool()
      throws Exception {
    final ObjectPoolHelper<Integer> pool = new ObjectPoolHelper<Integer>(new ObjectFactory<Integer>() {
      @Override
      public Integer create() {
        return 3;
      }
    });
    final GenericObjectPool<Integer> mockObjectPool = Mockito.mock(GenericObjectPool.class);
    pool.setObjectPool(mockObjectPool);
    pool.getObject();
    Mockito.verify(mockObjectPool, Mockito.times(1)).borrowObject();
  }
}
