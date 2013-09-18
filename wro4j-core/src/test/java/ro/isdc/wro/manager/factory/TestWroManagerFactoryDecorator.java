package ro.isdc.wro.manager.factory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;

public class TestWroManagerFactoryDecorator {
  @Mock
  private WroManagerFactory decorated;
  private WroManagerFactoryDecorator victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    MockitoAnnotations.initMocks(this);
    when(decorated.create()).thenReturn(new WroManager.Builder().build());
    victim = new WroManagerFactoryDecorator(decorated);
  }

  @Test
  public void shouldInvokeCreateOnDecoratedFactory() {
    victim.create();
    verify(decorated).create();
  }

  @Test
  public void shouldInvokeDestroyOnDecoratedFactory() {
    victim.destroy();
    verify(decorated).destroy();
  }
}
