/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import org.junit.Test;



/**
 * Test the {@link JsHintMojo} class.
 *
 * @author Alex Objelean
 */
public class TestJsHintMojo extends AbstractTestLinterMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractSingleProcessorMojo newLinterMojo() {
    return new JsHintMojo();
  }



  @Test
  public void testMojoWithPropertiesSet()
    throws Exception {
    getMojo().setIgnoreMissingResources(true);
    getMojo().execute();
  }


  @Test
  public void testWroXmlWithInvalidResourcesAndIgnoreMissingResourcesTrue() throws Exception {
    setWroWithInvalidResources();
    getMojo().setIgnoreMissingResources(true);
    getMojo().execute();
  }

  @Test
  public void testResourceWithUndefVariables()
    throws Exception {
    getMojo().setTargetGroups("undef");
    getMojo().execute();
  }


  @Test
  public void testEmptyOptions()
    throws Exception {
    getMojo().setOptions("");
    getMojo().setTargetGroups("undef");
    getMojo().execute();
  }
}
