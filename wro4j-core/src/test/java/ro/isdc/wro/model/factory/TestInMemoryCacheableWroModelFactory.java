package ro.isdc.wro.model.factory;

import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.WroModel;

public class TestInMemoryCacheableWroModelFactory {

  private WroModelFactory cacheableModelFactory;

  @Before
  public void setUp() {
    final WroConfiguration configuration = new WroConfiguration();
    configuration.setModelUpdatePeriod(1);

    Context.set(Context.standaloneContext(), configuration);
  }

  @After
  public void tearDown() {
    if (cacheableModelFactory != null) {
      cacheableModelFactory.destroy();
    }
    Context.destroy();
  }

  @Test
  public void createReplacesExistingModel() throws Exception {
    final WroModel first = new WroModel();
    final WroModel second = new WroModel();
    final WroModel third = new WroModel();

    final WroModelFactory underlyingModelFactory = Mockito.mock(WroModelFactory.class);
    Mockito.when(underlyingModelFactory.create()).thenReturn(first, second, third);

    cacheableModelFactory = new InMemoryCacheableWroModelFactory(underlyingModelFactory);

    cacheableModelFactory.create();
    cacheableModelFactory.create();
    cacheableModelFactory.destroy();
    cacheableModelFactory.create();
    cacheableModelFactory.create();

    assertSame(second, cacheableModelFactory.create());

    cacheableModelFactory.destroy();
    assertSame(third, cacheableModelFactory.create());
  }

}
