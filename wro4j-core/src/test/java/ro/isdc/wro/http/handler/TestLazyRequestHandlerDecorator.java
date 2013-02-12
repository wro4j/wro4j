package ro.isdc.wro.http.handler;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestLazyRequestHandlerDecorator {
  private LazyRequestHandlerDecorator victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullInitializer() {
    new LazyRequestHandlerDecorator(null);
  }

  @Test
  public void test() {
    victim = new LazyRequestHandlerDecorator(new LazyInitializer<RequestHandler>() {
      @Override
      protected RequestHandler initialize() {
        return new CustomRequestHandler();
      }
    });
    WroTestUtils.createInjector().inject(victim);
    assertTrue(victim.isEnabled());
  }

  private static class CustomRequestHandler extends RequestHandlerSupport {
    @Inject
    private ReadOnlyContext context;
    @Override
    public boolean isEnabled() {
      return context.getConfig().isDebug();
    }
  }
}
