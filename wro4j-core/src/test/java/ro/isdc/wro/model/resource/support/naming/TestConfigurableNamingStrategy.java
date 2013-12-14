package ro.isdc.wro.model.resource.support.naming;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.util.WroTestUtils;


/**
 * @author Alex Objelean
 */
public class TestConfigurableNamingStrategy {
  @Mock
  private NamingStrategy mockNamingStrategy;
  private ConfigurableNamingStrategy victim;
  
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
    MockitoAnnotations.initMocks(this);
    victim = new ConfigurableNamingStrategy();
    Context.set(Context.standaloneContext());
    WroTestUtils.createInjector().inject(victim);
  }
  
  @After
  public void tearDown() {
    Context.unset();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotSetNullProperties() {
    victim.setProperties(null);
  }
  
  @Test
  public void shouldUseNoOpNamingStrategyByDefault() {
    Assert.assertSame(NoOpNamingStrategy.class, victim.getConfiguredStrategy().getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidAlias() {
    victim.setProperties(buildPropsForAlias("invalidStrategy"));
    victim.getConfiguredStrategy();
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidAliases() {
    victim.setProperties(buildPropsForAlias(NoOpNamingStrategy.ALIAS + ", invalidOne"));
    victim.getConfiguredStrategy();
  }
  
  @Test
  public void shouldUseNoOpStrategyForValidAlias() {
    shouldUseCorrectStrategyForValidAlias(NoOpNamingStrategy.class, NoOpNamingStrategy.ALIAS);
  }
  
  @Test
  public void shouldUseTimestampNamingStrategyForValidAlias() {
    shouldUseCorrectStrategyForValidAlias(TimestampNamingStrategy.class, TimestampNamingStrategy.ALIAS);
  }
  
  @Test
  public void shouldUseHashEncoderStrategyForValidAlias() {
    shouldUseCorrectStrategyForValidAlias(DefaultHashEncoderNamingStrategy.class,
        DefaultHashEncoderNamingStrategy.ALIAS);
  }
  
  private void shouldUseCorrectStrategyForValidAlias(final Class<?> strategyClass, final String alias) {
    victim.setProperties(buildPropsForAlias(alias));
    final NamingStrategy actual = victim.getConfiguredStrategy();
    Assert.assertSame(strategyClass, actual.getClass());
  }
  
  private Properties buildPropsForAlias(final String alias) {
    final Properties props = new Properties();
    props.setProperty(ConfigurableNamingStrategy.KEY, alias);
    return props;
  }
  
  @Test
  public void shouldUseOverridenNamingStrategyMap() {
    final String mockAlias = "mock";
    victim = new ConfigurableNamingStrategy() {
      @Override
      protected void overrideDefaultStrategyMap(final Map<String, NamingStrategy> map) {
        map.put(mockAlias, mockNamingStrategy);
      }
    };
    victim.setProperties(buildPropsForAlias(mockAlias));
    final NamingStrategy actual = victim.getConfiguredStrategy();
    Assert.assertSame(mockNamingStrategy, actual);
  }
  
  @Test
  public void shouldRenameWithConfiguredStrategy()
      throws Exception {
    final String orignalName = "original.js";
    Assert.assertEquals(orignalName, victim.rename(orignalName, null));
  }
}
