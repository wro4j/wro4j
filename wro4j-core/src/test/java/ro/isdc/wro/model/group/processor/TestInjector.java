/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.config.support.ContextPropagatingCallable;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.CopyrightKeeperProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 * @created 12 Dec 2011
 */
public class TestInjector {
  private Injector victim;

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
    initializeValidInjector();
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullMap() {
    victim = new Injector(null);
  }

  @Test
  public void shouldAcceptInjectInitializedManager() {
    initializeValidInjector();
  }

  private void initializeValidInjector() {
    victim = InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotInjectUnsupportedAndUnitializedType() {
    initializeValidInjector();
    final Object inner = new Object() {
      @Inject
      private Object object;
    };
    victim.inject(inner);
  }

  @Test
  public void shouldBeThreadSafe() throws Exception {
    initializeValidInjector();
    WroTestUtils.runConcurrently(new ContextPropagatingCallable<Void>(new Callable<Void>() {
      public Void call()
          throws Exception {
        victim.inject(new GroupsProcessor());
        return null;
      }
    }));
  }

  @Test
  public void shouldInjectSupportedType()
      throws Exception {
    initializeValidInjector();
    final Callable<?> inner = new Callable<Void>() {
      @Inject
      private LifecycleCallbackRegistry object;

      public Void call()
          throws Exception {
        Assert.assertNotNull(object);
        return null;
      }
    };
    victim.inject(inner);
    inner.call();
  }

  @Test
  public void shouldInjectContext()
      throws Exception {
    // Cannot reuse this part, because generic type is not inferred correctly at runtime
    final Callable<?> inner = new Callable<Void>() {
      @Inject
      private ReadOnlyContext object;

      public Void call()
          throws Exception {
        assertNotNull(object);
        return null;
      }
    };
    victim.inject(inner);
    inner.call();
  }

  @Test
  public void canInjectContextOutsideOfContextScope()
      throws Exception {
    // remove the context explicitly
    Context.unset();
    shouldInjectContext();
  }

  @Test
  public void shouldInjectWroConfiguration()
      throws Exception {
    final Callable<?> inner = new Callable<Void>() {
      @Inject
      private ReadOnlyContext context;

      public Void call()
          throws Exception {
        assertNotNull(context.getConfig());
        return null;
      }
    };
    victim.inject(inner);
    inner.call();
  }

  private class TestProcessor
      extends JSMinProcessor {
    @Inject
    private ReadOnlyContext context;
  }

  @Test
  public void shouldInjectDecoratedProcessor() {
    final TestProcessor testProcessor = new TestProcessor();
    final ResourcePreProcessor processor = CopyrightKeeperProcessorDecorator.decorate(testProcessor);

    final Injector injector = InjectorBuilder.create(new BaseWroManagerFactory()).build();
    injector.inject(processor);
    assertNotNull(testProcessor.context);
  }

  @Test(expected = WroRuntimeException.class)
  public void shouldNotInjectUnsupportedAndInitializedTypes() {
    final String initialValue = "initial";
    final Callable<?> object = new Callable<Void>() {
      @Inject
      String unsupportedInitializedType = initialValue;

      public Void call()
          throws Exception {
        assertEquals(initialValue, unsupportedInitializedType);
        return null;
      }
    };
    victim.inject(object);
  }

  @Test
  public void shouldNotChangeAfterInjectionSupportedNotNullObject()
      throws Exception {
    new Callable<Void>() {
      @Inject
      private final ResourcePreProcessor inner = new ResourcePreProcessor() {
        public void process(final Resource resource, final Reader reader, final Writer writer)
            throws IOException {
        }
      };

      public Void call()
          throws Exception {
        Assert.assertNotNull(inner);
        return null;
      }
    }.call();
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotInjectUnsupportedInnerObject()
      throws Exception {
    final Callable<?> outer = new Callable<Void>() {
      @Inject
      private final Callable<?> inner = new Callable<Void>() {
        @Inject
        UriLocatorFactory locatorFactory;

        public Void call()
            throws Exception {
          Assert.assertNotNull(locatorFactory);
          return null;
        }
      };

      public Void call()
          throws Exception {
        inner.call();
        return null;
      }
    };
    victim.inject(outer);
    outer.call();
  }
 
  @Test
  public void canInjectOutsideOfContext() {
    Context.unset();
    TestProcessor processor = new TestProcessor();
    victim.inject(processor);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotAccessConfigOutsideOfContext() {
    Context.unset();
    TestProcessor processor = new TestProcessor();
    victim.inject(processor);
    processor.context.getConfig();
  }

  @After
  public void tearDown() {
    Context.unset();
  }
}
