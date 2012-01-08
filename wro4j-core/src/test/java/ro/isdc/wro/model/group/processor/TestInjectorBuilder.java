/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.util.NamingStrategy;

/**
 * @author Alex Objelean
 * @created 8 Jan 2012
 */
public class TestInjectorBuilder {

  @Before
  public void setUp() {
    Context.set(Context.standaloneContext());
  }

  @Test(expected=NullPointerException.class)
  public void cannotAcceptNullWroManager() {
    new InjectorBuilder(null);
  }

  @Test(expected=NullPointerException.class)
  public void cannotAcceptWhenSettingNullWroManager() {
    new InjectorBuilder().setWroManager(null);
  }

  @Test
  public void shouldBuildInjectorWithValidWroManager() {
    final WroManager manager = new WroManager();
    final Injector injector = new InjectorBuilder(manager).build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertNull(sample.namingStrategy);
    Assert.assertNotNull(sample.preProcessorExecutor);
    Assert.assertNull(sample.processorsFactory);
    Assert.assertNull(sample.resourceLocatorFactor);
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
  }

  @Test
  public void shouldBuildValidInjectorWithDefaultConstructor() {
    final Injector injector = new InjectorBuilder().build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertNotNull(sample.namingStrategy);
    Assert.assertNotNull(sample.preProcessorExecutor);
    Assert.assertNotNull(sample.processorsFactory);
    Assert.assertNotNull(sample.resourceLocatorFactor);
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
  }

  @Test
  public void shouldBuildValidInjectorWithSomeFieldsSet() {
    final NamingStrategy namingStrategy = Mockito.mock(NamingStrategy.class);
    final PreProcessorExecutor preProcessorExecutor = Mockito.mock(PreProcessorExecutor.class);
    final ProcessorsFactory processorsFactory = Mockito.mock(ProcessorsFactory.class);
    final ResourceLocatorFactory resourceLocatorFactory = Mockito.mock(ResourceLocatorFactory.class);
    final Injector injector = new InjectorBuilder().setNamingStrategy(namingStrategy).setPreProcessorExecutor(
      preProcessorExecutor).setProcessorsFactory(processorsFactory).setResourceLocatorFactory(resourceLocatorFactory).build();
    Assert.assertNotNull(injector);

    final Sample sample = new Sample();
    injector.inject(sample);
    Assert.assertSame(namingStrategy, sample.namingStrategy);
    Assert.assertSame(preProcessorExecutor, sample.preProcessorExecutor);
    Assert.assertSame(processorsFactory, sample.processorsFactory);
    Assert.assertSame(resourceLocatorFactory, sample.resourceLocatorFactor);
    Assert.assertNotNull(sample.callbackRegistry);
    Assert.assertSame(injector, sample.injector);
    Assert.assertNotNull(sample.groupsProcessor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  private static class Sample {
    @Inject
    ResourceLocatorFactory resourceLocatorFactor;
    @Inject
    ProcessorsFactory processorsFactory;
    @Inject
    NamingStrategy namingStrategy;
    @Inject
    PreProcessorExecutor preProcessorExecutor;
    @Inject
    LifecycleCallbackRegistry callbackRegistry;
    @Inject
    Injector injector;
    @Inject
    GroupsProcessor groupsProcessor;
  }
}
