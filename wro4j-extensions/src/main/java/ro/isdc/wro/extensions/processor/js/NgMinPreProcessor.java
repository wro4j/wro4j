package ro.isdc.wro.extensions.processor.js;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SupportAware;

@SupportedResourceType(ResourceType.JS)
public class NgMinPreProcessor implements ResourcePreProcessor, SupportAware {

	public static final String ALIAS = "ngMin";
	private static final Logger LOG = LoggerFactory.getLogger(NgMinPreProcessor.class);

	/**
	 * 'ngmin' must be available on the $PATH, and can be installed via npm:
	 * 'npm install -g ngmin'
	 */
	private static final String NGMIN_COMMAND = "ngmin";

	@Override
	public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {

		final OutputStream out = new WriterOutputStream(writer);
		final InputStream in = new ReaderInputStream(reader);

		try {

			process(in, out);

		} catch (final IOException ex) {

			final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
			LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
					+ " resource, no processing applied...", ex);

			throw ex;
		}
	}

	private int process(final InputStream in, final OutputStream out) throws ExecuteException, IOException {
		final Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(out, null, in));
		return executor.execute(CommandLine.parse(NGMIN_COMMAND));
	}

	@Override
	public boolean isSupported() {

		try {
			final InputStream input = new ByteArrayInputStream("".getBytes("UTF-8"));
			final int result = process(input, null);

			if (result == 0) {
				LOG.debug("The {} processor is supported.", getClass().getName());
				return true;
			} else {
				LOG.debug("The {} processor is not supported, test returned non-0 exit code.");
			}
		} catch (final Exception e) {
			LOG.debug("The {} processor is not supported. Because: {}", getClass().getName(), e.getMessage());
		}

		return false;
	}

}
