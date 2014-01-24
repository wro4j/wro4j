package ro.isdc.wro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestWroRuntimeException {
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Test
  public void shouldPreserveOriginalExceptionMessageWhenWrap() {
    final String message = "someMessage";
    Exception e = new IllegalArgumentException(message);
    Exception result = WroRuntimeException.wrap(e);
    assertEquals(e.getMessage(), result.getMessage());
  }
  
  @Test
  public void shouldNotWrapWhenExceptionIsAWroRuntimeException() {
    final String message = "someMessage";
    Exception e = new WroRuntimeException(message);
    Exception result = WroRuntimeException.wrap(e);
    assertSame(e, result);
  }
}
