package ro.isdc.wro.model.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer;
import ro.isdc.wro.util.Transformer;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestDefaultWroModelFactory {
  private WroModelFactory victim;
  @Inject
  private ResourceAuthorizationManager authManager;
  @Inject
  private Injector injector;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
    // required to get the authManager and make assertions agains it.
    injector.inject(this);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void decoratedModelshouldBeThreadSafe()
      throws Exception {
    final List<Transformer<WroModel>> modelTransformers = new ArrayList<Transformer<WroModel>>();
    modelTransformers.add(new WildcardExpanderModelTransformer());

    victim = DefaultWroModelFactoryDecorator.decorate(new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        return TestXmlModelFactory.class.getResourceAsStream("wroWithWildcardResources.xml");
      };
    }, modelTransformers);
    WroTestUtils.init(victim);
    final WroModel expectedModel = victim.create();
    WroTestUtils.runConcurrently(new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        assertEquals(expectedModel, victim.create());
        return null;
      }
    }));
  }

  @Test
  public void shouldAuthorizeResourcesFromModelWhenInDebugMode() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(true);
    Context.get().setConfig(config);
    final String authorizedResourceUri = "/authorized.js";
    createSampleModel(authorizedResourceUri);

    assertTrue(authManager.isAuthorized(authorizedResourceUri));
    assertFalse(authManager.isAuthorized("/notAuthorized.js"));
  }

  @Test
  public void shouldNotAuthorizeResourcesFromModelWhenNotInDebugMode() {
    final WroConfiguration config = new WroConfiguration();
    config.setDebug(false);
    Context.get().setConfig(config);
    final String authorizedResourceUri = "/authorized.js";
    createSampleModel(authorizedResourceUri);

    assertFalse(authManager.isAuthorized(authorizedResourceUri));
    assertFalse(authManager.isAuthorized("/notAuthorized.js"));
  }

  /**
   * Instructs the victim to create the model with a single resource
   */
  private void createSampleModel(final String resourceUri) {
    final WroModel model = new WroModel().addGroup(new Group("group").addResource(Resource.create(resourceUri)));
    final WroModelFactory decorated = new WroModelFactory() {
      public WroModel create() {
        return model;
      }

      public void destroy() {
      }
    };
    victim = DefaultWroModelFactoryDecorator.decorate(decorated, Collections.EMPTY_LIST);
    injector.inject(victim);
    victim.create();
  }
  
  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }
  
}
