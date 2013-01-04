/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.FileUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.JsLintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate js scripts defined in wro model using <a href="http://jslint.com/">jsLint</a>.
 *
 * @goal jslint
 * @phase compile
 * @requiresDependencyResolution runtime
 * @author Alex Objelean
 * @created 19 Sept 2011
 * @since 1.4.2
 */
public class JsLintMojo
    extends AbstractSingleProcessorMojo {
  /**
   * File where the report will be written.
   *
   * @parameter default-value="${project.build.directory}/wro4j-reports/jslint.xml" expression="${reportFile}"
   * @optional
   */
  private File reportFile;
  /**
   * Contains errors found during jshint processing which will be reported eventually.
   */
  private LintReport<LinterError> lintReport;

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createResourceProcessor() {
    final ResourcePreProcessor processor = new JsLintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        getLog().info("processing resource: " + resource);
        if (resource != null) {
          getLog().info("processing resource: " + resource.getUri());
        }
        //use StringWriter to discard the merged processed result (linting is useful only for reporting errors).
        super.process(resource, reader, new StringWriter());
      }

      @Override
      protected void onException(final WroRuntimeException e) {
        JsLintMojo.this.onException(e);
      }

      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        final String errorMessage = String.format("%s errors found while processing resource: %s. Errors are: %s",
            e.getErrors().size(), resource, e.getErrors());
        getLog().error(errorMessage);
        // collect found errors
        lintReport.addReport(ResourceLintReport.create(resource.getUri(), e.getErrors()));
        if (!isFailNever()) {
          throw new WroRuntimeException("Errors found when validating resource: " + resource);
        }
      };

    }.setOptions(getOptions());
    return processor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onBeforeExecute() {
    lintReport = new LintReport<LinterError>();
    FileUtils.deleteQuietly(reportFile);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onAfterExecute() {
    if (reportFile != null) {
      try {
        reportFile.getParentFile().mkdirs();
        reportFile.createNewFile();
        getLog().debug("creating report at location: " + reportFile);
        ReportXmlFormatter.createForLinterError(lintReport, ReportXmlFormatter.FormatterType.JSLINT).write(
            new FileOutputStream(reportFile));
      } catch (final IOException e) {
        getLog().error("Could not create report file: " + reportFile, e);
      }
    }
  }

  /**
   * @VisibleForTesting
   */
  void setReportFile(final File reportFile) {
    this.reportFile = reportFile;
  }

  /**
   * Used by unit test to check if mojo doesn't fail.
   */
  void onException(final Exception e) {
  }
}
