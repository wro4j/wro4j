package ro.isdc.wro.model.factory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.WroModel;

public class TestScheduledWroModelFactory {

  private WroModelFactory scheduledModelFactory;

  @Before
  public void setUp() {
    WroConfiguration configuration = new WroConfiguration();
    configuration.setModelUpdatePeriod(1);

    Context.set(Context.standaloneContext(), configuration);
  }

  @After
  public void tearDown() {
    if (scheduledModelFactory != null) {
      scheduledModelFactory.destroy();
    }
    Context.destroy();
  }

  @Test
  public void testCreateReplacesExistingModel() throws Exception {
    WroModel first = new WroModel();
    WroModel second = new WroModel();

    WroModelFactory underlyingModelFactory = Mockito.mock(WroModelFactory.class);
    Mockito.when(underlyingModelFactory.create()).thenReturn(first, second);

    scheduledModelFactory = new ScheduledWroModelFactory(underlyingModelFactory);

    scheduledModelFactory.create();

    Thread.sleep(1500);
    assertSame(second, scheduledModelFactory.create());
  }

}
