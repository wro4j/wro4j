package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.google.common.css.DefaultExitCodeHandler;
import com.google.common.css.ExitCodeHandler;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.GssParserException;
import com.google.common.css.compiler.ast.PrintStreamErrorManager;
import com.google.common.css.compiler.commandline.DefaultCommandLineCompiler;

/**
 * Uses Google closure compiler for css minimization and optimization.
 */
@Minimize
@SupportedResourceType(ResourceType.CSS)
public class GssCompressorProcessor implements ResourcePostProcessor,
		ResourcePreProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(GssCompressorProcessor.class);

	private static class WroGssCompiler extends DefaultCommandLineCompiler {

		private WroGssCompiler(JobDescription job,
				ExitCodeHandler exitCodeHandler, ErrorManager errorManager) {
			super(job, exitCodeHandler, errorManager);
		}
	}

	private final ErrorManager errorManager;

	public GssCompressorProcessor() {
		this(new PrintStreamErrorManager(System.err));
	}

	/**
	 */
	public GssCompressorProcessor(final ErrorManager errorManager) {
		
		this.errorManager = Validate.notNull(errorManager);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(final Resource resource, final Reader reader,
			final Writer writer) throws IOException {
		final String content = IOUtils.toString(reader);
		try {

			try {
				String fileName = resource == null ? "default-gss.css" : FilenameUtils.getName(resource.getUri());
				
				JobDescription job = new JobDescriptionBuilder()
					.addInput(new SourceCode(fileName, content))
					
					// 
					.setSimplifyCss(true)
					
					// merge identical rules 
					.setEliminateDeadStyles(true)
					
					// allow -moz, -webkit, -o, ... 
					.setAllowUnrecognizedFunctions(true)
					
					.setAllowedNonStandardFunctions(Arrays.asList("-webkit-linear-gradient()", 
							"-webkit-gradient()", "from()", "to()"))
					
					.setProcessDependencies(true)
					
					.getJobDescription();

				WroGssCompiler compiler = new WroGssCompiler(job, new DefaultExitCodeHandler(),
						errorManager);
				
				String compiled = compiler.compile();

				if (errorManager.hasErrors()) {
					writer.write(content);
				} else {
					writer.write(compiled);
				}
			} catch (GssParserException gpx) {
				LOG.error(gpx.getMessage(), gpx);
				writer.write(content);
			}

		} finally {
			LOG.debug("finally");
			reader.close();
			writer.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(final Reader reader, final Writer writer)
			throws IOException {

		process(null, reader, writer);
	}

}
