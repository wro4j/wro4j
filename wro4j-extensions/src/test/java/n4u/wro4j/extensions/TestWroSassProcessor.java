package n4u.wro4j.extensions;

import org.junit.Test;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroTestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class TestWroSassProcessor {
    @Test
    public void testWroSassPreProcessor()
            throws IOException {
        final URL url = getClass().getResource(".");
        final ResourcePreProcessor processor = new SassCssProcessor();

        final File testFolder = new File(url.getFile(), "templates");
        final File expectedFolder = new File(url.getFile(), "results");
        WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
    }

    @Test
    public void testWroSassPostProcessor()
            throws IOException {
        final URL url = getClass().getResource(".");
        final ResourcePostProcessor processor = new SassCssProcessor();

        final File testFolder = new File(url.getFile(), "templates");
        final File expectedFolder = new File(url.getFile(), "results");
        WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
    }

}
