/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;
import ro.isdc.wro.maven.plugin.support.ProgressIndicator;
import ro.isdc.wro.model.resource.Resource;


/**
 * <p>Contains common behavior for mojos responsible for static code analysis, example: csslint, jslint, jshint.</p>
 *
 * <p>Type {@code <T>} indicates the type of lint errors reported by this mojo.</p>
 *
 * @author Alex Objelean
 * @since 1.6.3
 */
public abstract class AbstractLinterMojo<T>
    extends AbstractSingleProcessorMojo {
  /**
   * When true, all the plugin won't stop its execution and will log all found errors.
   *
   * @parameter default-value="false" property="failNever"
   * @optional
   */
  private boolean failNever;
  /**
   * Allows build to fail when the first error is encountered without searching for next errors. This flag is true by
   * default. When interested in all errors, set this flag to false.
   *
   * @parameter default-value="true" property="failFast"
   * @optional
   */
  private boolean failFast = true;
  /**
   * Counts maximum acceptable number of errors, useful for progressive code quality enhancement strategy.
   *
   * @parameter property="failThreshold"
   * @optional
   */
  private int failThreshold = 0;
  /**
   * Contains errors found during jslint processing which will be reported eventually.
   */
  private LintReport<T> lintReport;
  private ProgressIndicator progressIndicator;

  /**
   * Add a single report to the registry of found errors.
   *
   * @param report
   *          to add.
   */
  protected final void addReport(final ResourceLintReport<T> report) {
    lintReport.addReport(report);
  }

  @Override
  protected void onBeforeExecute() {
    progressIndicator = new ProgressIndicator(getLog());
    getLog().info("failNever: " + failNever);
    progressIndicator.reset();

    // validate report format before actual plugin execution (fail fast).
    validateReportFormat();
    lintReport = new LintReport<T>();
    FileUtils.deleteQuietly(getReportFile());
  }

  @Override
  protected void onAfterExecute() {
    super.onAfterExecute();
    progressIndicator.logSummary();
    generateReport();
    checkFailStatus();
  }

  private void generateReport() {
    if (shouldGenerateReport()) {
      try {
        getReportFile().getParentFile().mkdirs();
        getReportFile().createNewFile();
        getLog().debug("creating report at location: " + getReportFile());
        final FormatterType type = FormatterType.getByFormat(getReportFormat());
        if (type != null) {
          try (OutputStream reportFileStream = new FileOutputStream(getReportFile())) {
            createXmlFormatter(lintReport, type).write(reportFileStream);
          }
        }
      } catch (final IOException e) {
        getLog().error("Could not create report file: " + getReportFile(), e);
      }
    }
  }

  /**
   * Check whether the build should fail.
   */
  private void checkFailStatus() {
    if (!failFast && isStatusFailed()) {
      throw new WroRuntimeException("Build status: failed.");
    }
  }

  /**
   * @return an instance of {@link ReportXmlFormatter} responsible for generating lint report.
   */
  protected abstract ReportXmlFormatter createXmlFormatter(LintReport<T> lintReport, FormatterType type);

  /**
   * A method which should be invoked on each new resource processing, having as a side effect an increment of the
   * counter holding the number of total processed resources.
   */
  protected final void onProcessingResource(final Resource resource) {
    progressIndicator.onProcessingResource(resource);
  }

  /**
   * @return true if the build can fail due to found errors based on existing configuration.
   */
  protected final boolean isFailAllowed() {
    return failFast && isStatusFailed();
  }

  /**
   * @return the file where the report should be written.
   */
  protected abstract File getReportFile();

  /**
   * @return the preferred format of the report.
   */
  protected abstract String getReportFormat();

  protected final ProgressIndicator getProgressIndicator() {
    return progressIndicator;
  }

  /**
   * @return true if the build status is failed.
   */
  private boolean isStatusFailed() {
    final int foundErrors = progressIndicator.getTotalFoundErrors();
    return !failNever && foundErrors > 0 && (foundErrors >= failThreshold);
  }

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
   * Used by unit test to check if mojo doesn't fail.
   */
  void onException(final Exception e) {
  }

  /**
   * @param failNever
   *          the failFast to set.
   */
  @Override
  public void setFailNever(final boolean failNever) {
    this.failNever = failNever;
  }

  public void setFailThreshold(final int failThreshold) {
    this.failThreshold = failThreshold;
  }

  /**
   * @return the failNever flag.
   */
  @Override
  public boolean isFailNever() {
    return failNever;
  }

  /**
   * @return the failFast flag.
   */
  boolean isFailFast() {
    return failFast;
  }

  LintReport<T> getLintReport() {
    return lintReport;
  }

  /**
   * @param failFast
   *          flag to set.
   */
  public void setFailFast(final boolean failFast) {
    this.failFast = failFast;
  }
}
