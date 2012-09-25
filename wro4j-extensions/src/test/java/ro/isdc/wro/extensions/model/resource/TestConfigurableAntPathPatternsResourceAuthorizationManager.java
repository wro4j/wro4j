package ro.isdc.wro.extensions.model.resource;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestConfigurableAntPathPatternsResourceAuthorizationManager {
  private ConfigurableAntPathPatternsResourceAuthorizationManager victim;
  @Before
  public void setUp() {
    victim = new ConfigurableAntPathPatternsResourceAuthorizationManager();
  }
  
  @Test
  public void shoudNotAuthorizeAnyResourceByDefault() {
    assertFalse(victim.isAuthorized("/any")); 
  }
  
  @Test
  public void shoudAuthorizeResourceMatchingExistingPattern() {
    victim.setPatterns(Arrays.asList("classpath:com/site/**", "/a/b/c/**/*.js"));
    assertTrue(victim.isAuthorized("classpath:com/site/resource/a.js")); 
    assertTrue(victim.isAuthorized("/a/b/c/d/e.js"));
  }
}
