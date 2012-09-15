/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ro.isdc.wro.util.WroUtil;


/**
 * Test the {@link JsHintMojo} class.
 * 
 * @author Alex Objelean
 */
public class TestJsHintMojo
    extends AbstractTestLinterMojo {
  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractSingleProcessorMojo newLinterMojo() {
    return new JsHintMojo() {
      @Override
      void onException(final Exception e) {
        Assert.fail("Shouldn't fail. Exception message: " + e.getMessage());
      }
    };
  }
  
  @Test
  public void usePredefOptions()
      throws Exception {
    getMojo().setOptions("predef=['YUI','window','document','OnlineOpinion','xui']");
    getMojo().setTargetGroups("undef");
    getMojo().execute();
  }
  
  @Test
  public void testMojoWithPropertiesSet()
      throws Exception {
    getMojo().setIgnoreMissingResources(true);
    getMojo().execute();
  }
  
  @Test
  public void testWroXmlWithInvalidResourcesAndIgnoreMissingResourcesTrue()
      throws Exception {
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
  
  @Test
  public void shouldGenerateXmlReportFile()
      throws Exception {
    final File reportFile = WroUtil.createTempFile();
    try {
      ((JsHintMojo) getMojo()).setReportFile(reportFile);
      getMojo().setOptions("undef, browser");
      getMojo().setTargetGroups(null);
      getMojo().execute();
    } finally {
      System.out.println(IOUtils.toString(new FileInputStream(reportFile)));
      FileUtils.deleteQuietly(reportFile);
    }
  }
}
