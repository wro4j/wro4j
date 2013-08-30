package ro.isdc.wro;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestWroRuntimeException {
  @Test
  public void shouldPreserveOriginalExceptionMessageWhenWrap() {
    final String message = "someMessage";
    Exception e = new IllegalArgumentException(message);
    Exception result = WroRuntimeException.wrap(e);
    Assert.assertEquals(e.getMessage(), result.getMessage());
  }
  
  @Test
  public void shouldNotWrapWhenExceptionIsAWroRuntimeException() {
    final String message = "someMessage";
    Exception e = new WroRuntimeException(message);
    Exception result = WroRuntimeException.wrap(e);
    Assert.assertSame(e, result);
  }
}
