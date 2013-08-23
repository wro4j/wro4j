/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.output.NullWriter;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Maven plugin used to validate css code defined in wro model.
 *
 * @goal csslint
 * @phase compile
 * @requiresDependencyResolution runtime
 * @author Alex Objelean
 * @since 1.3.8
 * @created 20 Jun 2011
 */
public class CssLintMojo
    extends AbstractLinterMojo<CssLintError> {
  /**
   * File where the report will be written.
   *
   * @parameter default-value="${project.build.directory}/wro4j-reports/csslint.xml" expression="${reportFile}"
   * @optional
   */
  private File reportFile;
  /**
   * The preferred format of the report.
   *
   * @parameter expression="${reportFormat}"
   * @optional
   */
  private String reportFormat = FormatterType.LINT.getFormat();

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourcePreProcessor createResourceProcessor() {
    final ResourcePreProcessor processor = new CssLintProcessor() {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        getProgressIndicator().onProcessingResource(resource);
        // use StringWriter to discard the merged processed result (linting is useful only for reporting errors).
        super.process(resource, reader, new NullWriter());
      }

      @Override
      protected void onException(final WroRuntimeException e) {
        CssLintMojo.this.onException(e);
      }

      @Override
      protected void onCssLintException(final CssLintException e, final Resource resource) {
        getProgressIndicator().addFoundErrors(e.getErrors().size());
        getLog().error(
            e.getErrors().size() + " errors found while processing resource: " + resource.getUri() + " Errors are: "
                + e.getErrors());
        // collect found errors
        addReport(ResourceLintReport.create(resource.getUri(), e.getErrors()));
        if (!isFailNever()) {
          throw new WroRuntimeException("Errors found when validating resource: " + resource);
        }
      };
    }.setOptionsAsString(getOptions());
    return processor;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected ReportXmlFormatter createXmlFormatter(final LintReport<CssLintError> lintReport, final FormatterType type) {
    return ReportXmlFormatter.createForCssLintError(lintReport, type);
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
   * Used by unit test to check if mojo doesn't fail.
   */
  @Override
  void onException(final Exception e) {
  }
}
