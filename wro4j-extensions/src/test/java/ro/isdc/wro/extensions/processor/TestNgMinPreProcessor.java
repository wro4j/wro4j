package ro.isdc.wro.extensions.processor;

import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.extensions.processor.css.NodeLessCssProcessor;
import ro.isdc.wro.extensions.processor.js.NgMinPreProcessor;
import ro.isdc.wro.util.WroTestUtils;

public class TestNgMinPreProcessor {

	private static boolean isSupported = false;

	@BeforeClass
	public static void beforeClass() {
		isSupported = new NgMinPreProcessor().isSupported();
	}

	/**
	 * Checks if the test can be run by inspecting {@link NodeLessCssProcessor#isSupported()}
	 */
	@Before
	public void beforeMethod() {
		Context.set(Context.standaloneContext());
		assumeTrue(isSupported);
	}

	@After
	public void tearDown() {
		Context.unset();
	}

	@Test
	public void testFromReaderWriter() throws IOException {

		final FileReader reader = new FileReader(filename("ngmin/testInput.js"));
		final StringWriter writer = new StringWriter();

		new NgMinPreProcessor().process(null, reader, writer);

		final FileInputStream expected = new FileInputStream(filename("ngmin/testOutput.js"));
		final InputStream actual = new ByteArrayInputStream(writer.toString().getBytes());

		WroTestUtils.compare(expected, actual);
	}

	private String filename(final String location) {
		return getClass().getResource(location).getFile();
	}
}
