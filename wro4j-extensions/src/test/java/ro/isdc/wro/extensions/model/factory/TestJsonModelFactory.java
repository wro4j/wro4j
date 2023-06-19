/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.model.factory;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.util.WroTestUtils;


/**
 * Test {@link JsonModelFactory}
 *
 * @author Alex Objelean
 * @since 1.3.6
 */
public class TestJsonModelFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TestJsonModelFactory.class);
  private JsonModelFactory factory;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test(expected = WroRuntimeException.class)
  public void testInvalidStream()
      throws Exception {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        throw new IOException();
      };
    };
    factory.create();
  }

  @Test(expected = WroRuntimeException.class)
  public void testInvalidContent() {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        return new ByteArrayInputStream("".getBytes());
      };
    };
    Assert.assertNull(factory.create());
  }

  @Test
  public void createValidModel() {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        return TestGroovyModelFactory.class.getResourceAsStream("wro.json");
      };
    };
    final WroModel model = factory.create();
    Assert.assertNotNull(model);
    Assert.assertEquals(Arrays.asList("g1", "g2"), new WroModelInspector(model).getGroupNames());
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
      protected InputStream getModelResourceAsStream()
          throws IOException {
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

  @Test
  public void shouldBeThreadSafe()
      throws Exception {
    factory = new JsonModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        return TestGroovyModelFactory.class.getResourceAsStream("wro.json");
      };
    };
    WroTestUtils.init(factory);
    final WroModel expected = factory.create();
    WroTestUtils.runConcurrently(new Callable<Void>() {
      @Override
      public Void call()
          throws Exception {
        Assert.assertEquals(expected, factory.create());
        return null;
      }
    }, 10);
  }
}
