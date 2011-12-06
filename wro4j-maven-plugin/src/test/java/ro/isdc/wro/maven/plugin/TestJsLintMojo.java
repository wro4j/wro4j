/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import junit.framework.Assert;

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
    return new JsLintMojo() {
      @Override
      void onException(final Exception e) {
        Assert.fail("Shouldn't fail. Exception message: " + e.getMessage());
      }
    };
  }

  @Test
  public void usePredefOptions() throws Exception {
    getMojo().setOptions("predef=['YUI','window','document','OnlineOpinion','xui']");
    //ignore found linter errors
    getMojo().setFailNever(true);
    getMojo().setTargetGroups("undef");
    getMojo().execute();
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
