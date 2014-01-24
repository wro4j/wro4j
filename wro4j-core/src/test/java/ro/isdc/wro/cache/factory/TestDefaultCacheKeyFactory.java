package ro.isdc.wro.cache.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * @author Alex Objelean
 */
public class TestDefaultCacheKeyFactory {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private GroupExtractor mockGroupExtractor;
  private DefaultCacheKeyFactory victim;
  
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
    initMocks(this);
    Context.set(Context.standaloneContext());
    victim = new DefaultCacheKeyFactory();
    final WroManagerFactory managerFactory = new BaseWroManagerFactory().setGroupExtractor(mockGroupExtractor);
    InjectorBuilder.create(managerFactory).build().inject(victim);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test
  public void shouldHaveMinimizeEnabledByDefault() {
    assertEquals(true, Context.get().getConfig().isMinimizeEnabled());
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotBuildCacheKeyFromNullRequest() {
    victim.create(null);
  }
  
  @Test
  public void shouldCreateNullCacheKeyWhenRequestDoesNotContainEnoughInfo() {
    assertNull(victim.create(mockRequest));
  }
  
  @Test
  public void shouldCreateNullCacheKeyWhenRequestDoesNotContainResourceType() {
    when(mockGroupExtractor.getGroupName(mockRequest)).thenReturn("g1");
    when(mockGroupExtractor.getResourceType(mockRequest)).thenReturn(null);
    assertNull(victim.create(mockRequest));
  }
  
  @Test
  public void shouldCreateNullCacheKeyWhenRequestDoesNotGroupName() {
    when(mockGroupExtractor.getGroupName(mockRequest)).thenReturn(null);
    when(mockGroupExtractor.getResourceType(mockRequest)).thenReturn(ResourceType.CSS);
    assertNull(victim.create(mockRequest));
  }
  
  @Test
  public void shouldCreateValidCacheKeyWhenRequestContainsAllRequiredInfo() {
    when(mockGroupExtractor.isMinimized(mockRequest)).thenReturn(true);
    when(mockGroupExtractor.getGroupName(mockRequest)).thenReturn("g1");
    when(mockGroupExtractor.getResourceType(mockRequest)).thenReturn(ResourceType.CSS);
    assertEquals(new CacheKey("g1", ResourceType.CSS, true), victim.create(mockRequest));
  }
  
  @Test
  public void shouldHaveMinimizationTurnedOffWhenMinimizeEnabledIsFalse()
      throws IOException {
    when(mockGroupExtractor.isMinimized(mockRequest)).thenReturn(true);
    when(mockGroupExtractor.getGroupName(mockRequest)).thenReturn("g1");
    when(mockGroupExtractor.getResourceType(mockRequest)).thenReturn(ResourceType.CSS);
    Context.get().getConfig().setMinimizeEnabled(false);
    assertEquals(new CacheKey("g1", ResourceType.CSS, false), victim.create(mockRequest));
  }
}
