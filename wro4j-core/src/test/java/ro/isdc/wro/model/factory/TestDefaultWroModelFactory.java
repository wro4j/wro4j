package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ContextPropagatingCallable;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.resource.locator.ResourceLocator;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;
import ro.isdc.wro.model.transformer.WildcardExpanderModelTransformer;
import ro.isdc.wro.util.Transformer;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestDefaultWroModelFactory {
  private WroModelFactory factory;
  
  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @Test
  public void decoratedModelshouldBeThreadSafe()
      throws Exception {
    final List<Transformer<WroModel>> modelTransformers = new ArrayList<Transformer<WroModel>>();
    modelTransformers.add(new WildcardExpanderModelTransformer());

    factory = new DefaultWroModelFactoryDecorator(new XmlModelFactory() {
      @Override
      protected ResourceLocator getModelResourceLocator() {
        return new UrlResourceLocator(TestXmlModelFactory.class.getResource("wroWithWildcardResources.xml"));
      };
    }, modelTransformers);
    WroTestUtils.init(factory);
    final WroModel expectedModel = factory.create();
    WroTestUtils.runConcurrently(new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        Assert.assertEquals(expectedModel, factory.create());
        return null;
      }
    }));
  }
}
