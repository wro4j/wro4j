package ro.isdc.wro.extensions.processor;

import org.junit.Test;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class TestRubySassProcessor {
    @Test
    public void testRubySassPreProcessor()
            throws IOException {
        final URL url = getClass().getResource("rubysass");
        final ResourcePreProcessor processor = new SassCssProcessor(SassCssProcessor.Engines.RUBY);

        final File testFolder = new File(url.getFile(), "templates");
        final File expectedFolder = new File(url.getFile(), "results");
        WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
    }

    @Test
    public void testRubySassPostProcessor()
            throws IOException {
        final URL url = getClass().getResource("rubysass");
        final ResourcePostProcessor processor = new SassCssProcessor(SassCssProcessor.Engines.RUBY);

        final File testFolder = new File(url.getFile(), "templates");
        final File expectedFolder = new File(url.getFile(), "results");
        WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
    }

}
