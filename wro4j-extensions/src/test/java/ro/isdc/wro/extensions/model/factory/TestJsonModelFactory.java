/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link JsonModelFactory}
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 */
public class TestJsonModelFactory {
  private JsonModelFactory factory;
  @Before
  public void setUp() {
    factory = new JsonModelFactory();
  }

  @Test
  public void test() {
    factory = new JsonModelFactory();
    factory.getInstance();
  }
}
