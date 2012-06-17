package ro.isdc.wro.model.resource.support.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.isdc.wro.WroRuntimeException;


/**
 * @author Alex Objelean
 */
public class TestConfigurableNamingStrategy {
  @Mock
  private NamingStrategy mockNamingStrategy;
  private ConfigurableNamingStrategy victim;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    victim = new ConfigurableNamingStrategy();
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotSetNullProperties() {
    victim.setProperties(null);
  }
  
  @Test
  public void shouldUseNoOpNamingStrategyByDefault() {
    Assert.assertSame(NoOpNamingStrategy.class, victim.getConfiguredNamingStrategy().getClass());
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidAlias() {
    victim.setProperties(buildPropsForAlias("invalidStrategy")).getConfiguredNamingStrategy();
  }
  
  @Test(expected = WroRuntimeException.class)
  public void cannotConfigureInvalidAliases() {
    victim.setProperties(buildPropsForAlias(NoOpNamingStrategy.ALIAS + ", invalidOne")).getConfiguredNamingStrategy();
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
    shouldUseCorrectStrategyForValidAlias(HashEncoderNamingStrategy.class, HashEncoderNamingStrategy.ALIAS);
  }
  
  private void shouldUseCorrectStrategyForValidAlias(final Class<?> strategyClass, final String alias) {
    final NamingStrategy actual = victim.setProperties(buildPropsForAlias(alias)).getConfiguredNamingStrategy();
    Assert.assertSame(strategyClass, actual.getClass());
  }
  
  private Properties buildPropsForAlias(final String alias) {
    final Properties props = new Properties();
    props.setProperty(ConfigurableNamingStrategy.PARAM_NAMING_STRATEGY, alias);
    return props;
  }
  
  @Test
  public void shouldUseOverridenNamingStrategyMap() {
    final String mockAlias = "mock";
    victim = new ConfigurableNamingStrategy() {
      @Override
      protected Map<String, NamingStrategy> newNamingStrategyMap() { 
        final Map<String, NamingStrategy> map = new HashMap<String, NamingStrategy>();
        map.put(mockAlias, mockNamingStrategy);
        return map;
      }
    };
    final NamingStrategy actual = victim.setProperties(buildPropsForAlias(mockAlias)).getConfiguredNamingStrategy();
    Assert.assertSame(mockNamingStrategy, actual);
  }
  
  @Test
  public void shouldRenameWithConfiguredStrategy()
      throws Exception {
    final String orignalName = "original.js";
    Assert.assertEquals(orignalName, victim.rename(orignalName, null));
  }
}
