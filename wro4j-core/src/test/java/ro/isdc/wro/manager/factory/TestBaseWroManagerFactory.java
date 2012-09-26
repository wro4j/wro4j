/**
 * Copyright@2011 wro4j
 */
package ro.isdc.wro.manager.factory;

import static org.mockito.Mockito.verify;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallback;
import ro.isdc.wro.manager.callback.LifecycleCallbackSupport;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.support.naming.NoOpNamingStrategy;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestBaseWroManagerFactory {
  @Mock
  private WroModelFactory mockModelFactory;
  @Mock
  private CacheStrategy<CacheEntry, ContentHashEntry> mockCacheStrategy;
  private BaseWroManagerFactory victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new BaseWroManagerFactory();
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test
  public void defaultModelFactoryIsXml() {
    new BaseWroManagerFactory() {
      @Override
      protected WroModelFactory newModelFactory() {
        final WroModelFactory modelFactory = super.newModelFactory();
        Assert.assertEquals(XmlModelFactory.class, modelFactory.getClass());
        return modelFactory;
      }
    };
  }
  
  @Test
  public void shouldCreateManager()
      throws Exception {
    final WroManager manager = victim.create();
    Assert.assertNotNull(manager);
    Assert.assertEquals(NoOpNamingStrategy.class, manager.getNamingStrategy().getClass());
  }
  
  @Test
  public void shouldSetCallback()
      throws Exception {
    final LifecycleCallback callback = Mockito.spy(new LifecycleCallbackSupport());
    victim = new BaseWroManagerFactory().setModelFactory(WroUtil.factoryFor(new WroModel()));
    final WroManager manager = victim.create();
    InjectorBuilder.create(victim).build().inject(manager);
    
    manager.registerCallback(callback);
    manager.getModelFactory().create();
    
    Mockito.verify(callback).onBeforeModelCreated();
    Mockito.verify(callback).onAfterModelCreated();
  }
  
  @Test
  public void shouldNotFailWhenReloadingModelOutsideOfContext()
      throws Exception {
    Context.unset();
    victim.onModelPeriodChanged(0);
  }
  
  @Test
  public void shouldNotFailWhenReloadingCacheOutsideOfContext()
      throws Exception {
    Context.unset();
    victim.onCachePeriodChanged(0);
  }
  
  @Test
  public void shouldReloadOnlyModelWhenClearModelIsInvoked()
      throws Exception {
    victim = new BaseWroManagerFactory().setModelFactory(mockModelFactory).setCacheStrategy(mockCacheStrategy);
    final WroManager manager = victim.create();
    
    manager.onModelPeriodChanged(0);
    
    Context.get().getConfig().reloadModel();
    verify(mockModelFactory, Mockito.times(1)).destroy();
    verify(mockCacheStrategy, Mockito.never()).clear();
  }
  
  @Test
  public void shouldReloadOnlyCacheWhenClearCacheIsInvoked()
      throws Exception {
    victim = new BaseWroManagerFactory().setModelFactory(mockModelFactory).setCacheStrategy(mockCacheStrategy);
    final WroManager manager = victim.create();
    
    manager.onCachePeriodChanged(0);
    
    Context.get().getConfig().reloadCache();
    verify(mockModelFactory, Mockito.never()).destroy();
    verify(mockCacheStrategy, Mockito.times(1)).clear();
  }
}
