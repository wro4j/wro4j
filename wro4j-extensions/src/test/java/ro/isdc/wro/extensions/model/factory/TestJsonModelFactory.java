/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;

/**
 * Test {@link JsonModelFactory}
 *
 * @author Alex Objelean
 * @created 13 Mar 2011
 * @since 1.3.6
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
    factory.create();
  }

  @Test(expected=WroRuntimeException.class)
  public void testInvalidContent() {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getWroModelStream() throws IOException {
        return new ByteArrayInputStream("".getBytes());
      };
    };
    Assert.assertNull(factory.create());
  }

  @Test
  public void createValidModel() {
    factory = new JsonModelFactory();
    final WroModel model = factory.create();
    Assert.assertNotNull(model);
    Assert.assertEquals(Arrays.asList("g2", "g1"), model.getGroupNames());
    LOG.debug("model: {}", model);
  }


  /**
   * Test the usecase when the resource has no type. For now, it is ok to have it null because you'll get a NPE
   * exception during processing if no type is specified anyway.
   */
  @Test
  public void createIncompleteModel() {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getWroModelStream() throws IOException {
        return getClass().getResourceAsStream("incomplete-wro.json");
      };
    };
    final WroModel model = factory.create();
    Assert.assertNotNull(model);
    Assert.assertEquals(1, model.getGroups().size());
    final Group group = new ArrayList<Group>(model.getGroups()).get(0);
    Assert.assertNull(group.getResources().get(0).getType());
    LOG.debug("model: {}", model);
  }
}
