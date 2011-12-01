/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.css.GssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.util.WroTestUtils;

/**
 * Test Google StyleSheet css compressor processor.
 */
public class TestGssCompressorProcessor {
	@Test
	public void shouldMininimizeCss() throws IOException {

		final ResourcePostProcessor processor = new GssCompressorProcessor();
		final URL url = getClass().getResource("gss");

		final File testFolder = new File(url.getFile(), "test");
		final File expectedFolder = new File(url.getFile(), "expected");
		WroTestUtils.compareFromDifferentFoldersByExtension(testFolder,
				expectedFolder, "css", processor);
	}
}
