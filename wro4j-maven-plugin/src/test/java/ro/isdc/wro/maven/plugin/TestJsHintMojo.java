/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * Test the {@link JsHintMojo} class.
 *
 * @author Alex Objelean
 */
public class TestJsHintMojo
    extends AbstractTestLinterMojo {
  @Override
  protected AbstractLinterMojo newLinterMojo() {
    return new JsHintMojo() {
      @Override
      void onException(final Exception e) {
        fail("Shouldn't fail. Exception message: " + e.getMessage());
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
  public void shouldProcessMultipleGroupsMore()
      throws Exception {
    for (int i = 0; i < 10; i++) {
      shouldProcessMultipleGroups();
    }
  }
  
  @Test
  public void shouldProcessMultipleGroups()
      throws Exception {
    getMojo().setTargetGroups("undef,valid,g3");
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
      getMojo().setFailNever(true);
      getMojo().setIgnoreMissingResources(true);
      getMojo().execute();
    } finally {
      // Assert that file is big enough to prove that it contains serialized errors.
      assertTrue(reportFile.length() > 1000);
      FileUtils.deleteQuietly(reportFile);
    }
  }
  
  @Test
  public void shouldGenerateReportWithDefaultFormat()
      throws Exception {
    generateAndCompareReportFile(null, "jshint-default.xml");
  }
  
  @Test
  public void shouldGenerateReportWithCheckstyleFormat()
      throws Exception {
    generateAndCompareReportFile(FormatterType.CHECKSTYLE.getFormat(), "jshint-checkstyle.xml");
  }
  
  @Test(expected = MojoExecutionException.class)
  public void cannotGenerateReportWithInvalidFormat()
      throws Exception {
    final File reportFile = WroUtil.createTempFile();
    final JsHintMojo mojo = (JsHintMojo) getMojo();
    try {
      mojo.setReportFile(reportFile);
      mojo.setReportFormat("INVALID");
      mojo.setOptions("undef, browser");
      mojo.setTargetGroups(null);
      mojo.setFailNever(true);
      mojo.setIgnoreMissingResources(true);
      mojo.execute();
    } finally {
      FileUtils.deleteQuietly(reportFile);
    }
  }
  
  @Test
  public void shouldNotFailWhenThresholdIsGreaterThanNumberOfErrors()
      throws Exception {
    final JsHintMojo jsHintMojo = (JsHintMojo) getMojo();
    jsHintMojo.setFailThreshold(6);
    executeResourcesWithErrors();
  }
  
  @Test(expected = MojoExecutionException.class)
  public void shouldFailWhenThresholdEqualsWithNumberOfErrors()
      throws Exception {
    final JsHintMojo jsHintMojo = (JsHintMojo) getMojo();
    jsHintMojo.setFailThreshold(5);
    executeResourcesWithErrors();
  }
  
  /**
   * Checks that build doesn't fail when the failFast is true and there is no resources to be processed.
   */
  @Test
  public void shouldNotFailWhenNoErrorsFound()
      throws Exception {
    final JsHintMojo jsHintMojo = (JsHintMojo) getMojo();
    jsHintMojo.setFailThreshold(0);
    jsHintMojo.setFailFast(false);
    jsHintMojo.setIgnoreMissingResources(true);
    jsHintMojo.setTargetGroups("invalidWildcardResource");
    getMojo().execute();
  }
  
  @Test(expected = MojoExecutionException.class)
  public void shouldReportOnlyFirstErrorWhenFailFastIsTrue()
      throws Exception {
    final JsHintMojo jsHintMojo = (JsHintMojo) getMojo();
    jsHintMojo.setFailFast(true);
    try {
      executeResourcesWithErrors();
    } finally {
      final LintReport<?> lintReport = jsHintMojo.getLintReport();
      assertEquals(1, lintReport.getReports().size());
    }
  }
  
  @Test(expected = MojoExecutionException.class)
  public void shouldReportAllErrorsWhenFailFastIsFalse()
      throws Exception {
    final JsHintMojo jsHintMojo = (JsHintMojo) getMojo();
    jsHintMojo.setFailFast(false);
    try {
      executeResourcesWithErrors();
    } catch (final Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      final LintReport<?> lintReport = jsHintMojo.getLintReport();
      assertEquals(2, lintReport.getReports().size());
    }
  }
  
  private void executeResourcesWithErrors()
      throws MojoExecutionException {
    getMojo().setTargetGroups("invalidResources");
    getMojo().setOptions("undef, browser");
    getMojo().execute();
  }
  
  private void generateAndCompareReportFile(final String reportFormat, final String expectedFileName)
      throws Exception {
    final File reportFile = WroUtil.createTempFile();
    final JsHintMojo mojo = (JsHintMojo) getMojo();
    try {
      mojo.setReportFile(reportFile);
      if (reportFormat != null) {
        mojo.setReportFormat(reportFormat);
      }
      mojo.setOptions("undef, browser");
      mojo.setTargetGroups(null);
      mojo.setFailNever(true);
      mojo.setIgnoreMissingResources(true);
      mojo.execute();
    } finally {
      // Assert that file is big enough to prove that it contains serialized errors.
      WroTestUtils.compare(getClass().getResourceAsStream("report/" + expectedFileName),
          new FileInputStream(reportFile));
      FileUtils.deleteQuietly(reportFile);
    }
  }
  
  @Test(expected = LinterException.class)
  public void shouldFailWhenThereAreLinterErrorsEvenWhenIncrementBuildIsEnabled()
      throws Throwable {
    getMojo().setParallelProcessing(true);
    getMojo().setIncrementalBuildEnabled(true);
    getMojo().setTargetGroups("invalidResources");
    try {
      getMojo().execute();
    } catch (final MojoExecutionException e) {
      assertTrue(e.getCause() instanceof LinterException);
      try {
        getMojo().execute();
      } catch (final MojoExecutionException secondException) {
        throw secondException.getCause();
      }
    } finally {
      getMojo().clean();
    }
  }
}
