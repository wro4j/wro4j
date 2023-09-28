/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.output.NullWriter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.extensions.support.lint.LintReport;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter;
import ro.isdc.wro.extensions.support.lint.ReportXmlFormatter.FormatterType;
import ro.isdc.wro.extensions.support.lint.ResourceLintReport;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Maven plugin used to validate js scripts defined in wro model using
 * <a href="http://jshint.com/">jsLint</a>.
 *
 * @author Alex Objelean
 * @author Paul Podgorsek
 * @since 1.3.5
 */
@Mojo(name = "jshint", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JsHintMojo extends AbstractLinterMojo<LinterError> {

	/**
	 * File where the report will be written.
	 */
	@Parameter(defaultValue = "${project.build.directory}/wro4j-reports/jshint.xml")
	private File reportFile;

	/**
	 * The preferred format of the report.
	 */
	@Parameter
	private String reportFormat = FormatterType.JSLINT.getFormat();

	@Override
	protected ResourcePreProcessor createResourceProcessor() {
		return new JsHintProcessor() {
			@Override
			public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
				getProgressIndicator().onProcessingResource(resource);
				// use StringWriter to discard the merged processed result (linting is useful
				// only for reporting errors).
				super.process(resource, reader, new NullWriter());
			}

			@Override
			protected void onException(final WroRuntimeException e) {
				JsHintMojo.this.onException(e);
			}

			@Override
			protected void onLinterException(final LinterException e, final Resource resource) {
				final String errorMessage = String.format(
						"%s errors found while processing resource: %s. Errors are: %s", e.getErrors().size(), resource,
						e.getErrors());
				getProgressIndicator().addFoundErrors(e.getErrors().size());
				getLog().error(errorMessage);
				// collect found errors
				addReport(ResourceLintReport.create(resource.getUri(), e.getErrors()));
				if (isFailAllowed()) {
					getLog().error("Errors found when validating resource: " + resource);
					throw e;
				}
			};
		}.setOptionsAsString(getOptions());
	}

	@Override
	protected boolean wantProcessGroup(final String groupName, final ResourceType resourceType) {
		return resourceType == ResourceType.JS;
	}

	@Override
	protected ReportXmlFormatter createXmlFormatter(final LintReport<LinterError> lintReport,
			final FormatterType type) {
		return ReportXmlFormatter.createForLinterError(lintReport, type);
	}

	@Override
	protected File getReportFile() {
		return reportFile;
	}

	@Override
	protected String getReportFormat() {
		return reportFormat;
	}

	void setReportFile(final File reportFile) {
		this.reportFile = reportFile;
	}

	/**
	 * @param reportFormat the preferred report format.
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
