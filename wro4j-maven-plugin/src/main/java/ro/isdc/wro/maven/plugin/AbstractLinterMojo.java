/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;


/**
 * Contains common behavior for mojos responsible for static code analysis, example: csslint, jslint, jshint.
 *
 * @author Alex Objelean
 * @created 25 Jan 2013
 * @since 1.6.3
 */
public abstract class AbstractLinterMojo<T>
    extends AbstractSingleProcessorMojo {
  /**
   * Contains errors found during jslint processing which will be reported eventually.
   */
  private LintReport<T> lintReport;

  /**
   * Add a single report to the registry of found errors.
   *
   * @param report
   *          to add.
   */
  protected final void addReport(final ResourceLintReport report) {
    lintReport.addReport(report);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onBeforeExecute() {
    // validate report format before actual plugin execution (fail fast).
    validateReportFormat();
    lintReport = new LintReport<T>();
    FileUtils.deleteQuietly(getReportFile());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onAfterExecute() {
    if (shouldGenerateReport()) {
      try {
        getReportFile().getParentFile().mkdirs();
        getReportFile().createNewFile();
        getLog().debug("creating report at location: " + getReportFile());
        final FormatterType type = FormatterType.getByFormat(getReportFormat());
        if (type != null) {
          createXmlFormatter(lintReport, type).write(new FileOutputStream(getReportFile()));
        }
      } catch (final IOException e) {
        getLog().error("Could not create report file: " + getReportFile(), e);
      }
    }
  }

  protected abstract ReportXmlFormatter createXmlFormatter(LintReport<T> lintReport, FormatterType type);

  private void validateReportFormat() {
    if (FormatterType.getByFormat(getReportFormat()) == null) {
      throw new WroRuntimeException("Usupported report format: " + getReportFormat() + ". Valid formats are: "
          + FormatterType.getSupportedFormatsAsCSV());
    }
  }

  private boolean shouldGenerateReport() {
    return getReportFile() != null;
  }

  /**
   * @return the file where the report should be written.
   */
  protected abstract File getReportFile();

  /**
   * @return the preferred format of the report.
   */
  protected abstract String getReportFormat();

  /**
   * Used by unit test to check if mojo doesn't fail.
   * @VisibleForTesting
   */
  void onException(final Exception e) {
  }
}
