/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.util.ObjectFactory;

/**
 * @author Alex Objelean
 */
public class TestObjectPoolHelper {
  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullArgument() throws Exception {
    new ObjectPoolHelper<Void>(null);
  }

  @Test(expected=NullPointerException.class)
  public void cannotReturnNullObjectToPool() throws Exception {
    final ObjectPoolHelper<Integer> pool = new ObjectPoolHelper<Integer>(new ObjectFactory<Integer>() {
      @Override
      public Integer create() {
        return null;
      }
    });
    final Integer object = pool.getObject();
    pool.returnObject(object);
  }

  public void shouldReuseExistingObject() throws Exception {
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
}
