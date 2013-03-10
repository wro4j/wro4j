/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate js scripts defined in wro model using <a href="http://jshint.com/">jsLint</a>.
 *
 * @goal jshint
 * @phase compile
 * @requiresDependencyResolution runtime
 * @author Alex Objelean
 * @since 1.3.5
 */
public class JsHintMojo
    extends AbstractLinterMojo<LinterError> {
  /**
   * File where the report will be written.
   *
   * @parameter default-value="${project.build.directory}/wro4j-reports/jshint.xml" expression="${reportFile}"
   * @optional
   */
  private File reportFile;
  /**
   * The preferred format of the report.
   *
   * @parameter expression="${reportFormat}"
   * @optional
   */
  private String reportFormat = FormatterType.JSLINT.getFormat();
  /**
   * Counts maximum acceptable number of jshint errors, useful for progressive code quality enhancement strategy.
   *
   * @parameter expression="${failThreshold}"
   * @optional
   */
  private int failThreshold = 0;
  /**
   * Counts total number of processed resources.
   */
  private int totalResources = 0;
  /**
   * Counts total number of resources with errors.
   */
  private int totalResourcesWithErrors = 0;
  /**
   * Counts total number of jshint errors.
   */
  private int totalFoundErrors = 0;

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createResourceProcessor() {
    final ResourcePreProcessor processor = new JsHintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        totalResources++;
        getLog().info("processing resource: " + resource);
        // use StringWriter to discard the merged processed result (linting is useful only for reporting errors).
        super.process(resource, reader, new StringWriter());
      }

      @Override
      protected void onException(final WroRuntimeException e) {
        JsHintMojo.this.onException(e);
      }

      @Override
      protected void onLinterException(final LinterException e, final Resource resource) {
        final String errorMessage = String.format("%s errors found while processing resource: %s. Errors are: %s", e
            .getErrors().size(), resource, e.getErrors());
        totalResourcesWithErrors++;
        totalFoundErrors += e.getErrors().size();
        getLog().error(errorMessage);
        // collect found errors
        addReport(ResourceLintReport.create(resource.getUri(), e.getErrors()));
        if (!isFailNever()) {
          if (totalFoundErrors >= failThreshold) {
            throw new WroRuntimeException("Errors found when validating resource: " + resource);
          }
        }
      };
    }.setOptionsAsString(getOptions());
    return processor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onBeforeExecute() {
    totalFoundErrors = 0;
    totalResources = 0;
    totalResourcesWithErrors = 0;
    super.onBeforeExecute();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onAfterExecute() {
    logSummary();
    super.onAfterExecute();
  }

  private void logSummary() {
    final String message = totalFoundErrors == 0 ? "JSHINT found no errors." : String.format(
        "JSHINT found %s errors in %s files.", totalFoundErrors, totalResourcesWithErrors);
    getLog().info("----------------------------------------");
    getLog().info(String.format("Total number of processed resources: %s", totalResources));
    getLog().info(message);
    getLog().info("----------------------------------------\n");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ReportXmlFormatter createXmlFormatter(final LintReport<LinterError> lintReport, final FormatterType type) {
    return ReportXmlFormatter.createForLinterError(lintReport, type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected File getReportFile() {
    return reportFile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFormat() {
    return reportFormat;
  }

  /**
   * @VisibleForTesting
   */
  void setReportFile(final File reportFile) {
    this.reportFile = reportFile;
  }

  /**
   * @param reportFormat
   *          the preferred report format.
   * @VisibleForTesting
   */
  void setReportFormat(final String reportFormat) {
    this.reportFormat = reportFormat;
  }

  /**
   * @VisibleForTesting
   */
  void setFailThreshold(final int failThreshold) {
    this.failThreshold = failThreshold;
  }

  /**
   * Used by unit test to check if mojo doesn't fail.
   *
   * @VisibleForTesting
   */
  @Override
  void onException(final Exception e) {
  }
}
