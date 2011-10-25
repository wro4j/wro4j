/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;


/**
 * Test the {@link JsLintMojo} class.
 *
 * @author Alex Objelean
 */
public class TestJsLintMojo extends AbstractTestLinterMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractSingleProcessorMojo newLinterMojo() {
    return new JsLintMojo();
  }


  @Test(expected=MojoExecutionException.class)
  public void testMojoWithPropertiesSet()
    throws Exception {
    getMojo().setIgnoreMissingResources(true);
    getMojo().execute();
  }


  @Test(expected=MojoExecutionException.class)
  public void testWroXmlWithInvalidResourcesAndIgnoreMissingResourcesTrue() throws Exception {
    setWroWithInvalidResources();
    getMojo().setIgnoreMissingResources(true);
    getMojo().execute();
  }

  @Test(expected=MojoExecutionException.class)
  public void testResourceWithUndefVariables()
    throws Exception {
    getMojo().setTargetGroups("undef");
    getMojo().execute();
  }


  @Test(expected=MojoExecutionException.class)
  public void testEmptyOptions()
    throws Exception {
    getMojo().setOptions("");
    getMojo().setTargetGroups("undef");
    getMojo().execute();
  }
}
