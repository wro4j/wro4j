/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;

/**
 * Test {@link JsonModelFactory}
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 */
public class TestJsonModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestJsonModelFactory.class);
  private JsonModelFactory factory;

  @Test(expected=WroRuntimeException.class)
  public void testInvalidStream() throws Exception {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getWroModelStream()
        throws IOException {
        throw new IOException();
      };
    };
    factory.getInstance();
  }

  @Test(expected=WroRuntimeException.class)
  public void testInvalidContent() {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getWroModelStream() throws IOException {
        return new ByteArrayInputStream("".getBytes());
      };
    };
    Assert.assertNull(factory.getInstance());
  }

  @Test
  public void test() {
    factory = new JsonModelFactory();
    factory.getInstance();
  }
}
