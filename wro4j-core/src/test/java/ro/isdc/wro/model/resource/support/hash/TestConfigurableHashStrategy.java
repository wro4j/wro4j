package ro.isdc.wro.model.resource.support.hash;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
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
public class TestConfigurableHashStrategy {
  
  @Mock
  private HashStrategy mockHashStrategy;
  
  private ConfigurableHashStrategy victim;
  
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
    victim = new ConfigurableHashStrategy();
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
    Assert.assertSame(SHA1HashStrategy.class, victim.getConfiguredStrategy().getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidAlias() {
    victim.setProperties(buildPropsForAlias("invalidStrategy"));
    victim.getConfiguredStrategy();
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidAliases() {
    victim.setProperties(buildPropsForAlias(CRC32HashStrategy.ALIAS + ", invalidOne"));
    victim.getConfiguredStrategy();
  }
  
  @Test
  public void shouldUseCRC32StrategyForValidAlias() {
    shouldUseCorrectStrategyForValidAlias(CRC32HashStrategy.class, CRC32HashStrategy.ALIAS);
  }
  
  @Test
  public void shouldUseTimestampNamingStrategyForValidAlias() {
    shouldUseCorrectStrategyForValidAlias(MD5HashStrategy.class, MD5HashStrategy.ALIAS);
  }
  
  @Test
  public void shouldUseHashEncoderStrategyForValidAlias() {
    shouldUseCorrectStrategyForValidAlias(SHA1HashStrategy.class, SHA1HashStrategy.ALIAS);
  }
  
  private void shouldUseCorrectStrategyForValidAlias(final Class<?> strategyClass, final String alias) {
    victim.setProperties(buildPropsForAlias(alias));
    final HashStrategy actual = victim.getConfiguredStrategy();
    Assert.assertSame(strategyClass, actual.getClass());
  }
  
  private Properties buildPropsForAlias(final String alias) {
    final Properties props = new Properties();
    props.setProperty(ConfigurableHashStrategy.KEY, alias);
    return props;
  }
  
  @Test
  public void shouldUseOverridenStrategyMap() {
    final String mockAlias = "mock";
    victim = new ConfigurableHashStrategy() {
      @Override
      protected void overrideDefaultStrategyMap(final Map<String, HashStrategy> map) {
        map.put(mockAlias, mockHashStrategy);
      }
    };
    victim.setProperties(buildPropsForAlias(mockAlias));
    final HashStrategy actual = victim.getConfiguredStrategy();
    Assert.assertSame(mockHashStrategy, actual);
  }
  
  @Test
  public void shouldHashWithConfiguredStrategy()
      throws Exception {
    Assert.assertEquals("8151325dcdbae9e0ff95f9f9658432dbedfdb209",
        victim.getHash(new ByteArrayInputStream("sample".getBytes())));
  }
  
}
