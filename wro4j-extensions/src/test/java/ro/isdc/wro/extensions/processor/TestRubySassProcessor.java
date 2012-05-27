package ro.isdc.wro.extensions.processor;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.css.RubySassCssProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.util.WroTestUtils;


public class TestRubySassProcessor {
    @Test
    public void shouldProcessResourcesFromFolder()
            throws Exception {
        final URL url = getClass().getResource("rubysass");
        final ResourceProcessor processor = new RubySassCssProcessor();

        final File testFolder = new File(url.getFile(), "templates");
        final File expectedFolder = new File(url.getFile(), "results");
        WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "css", processor);
    }


    @Test
    public void shouldBeThreadSafe() throws Exception {
      final ResourceProcessor processor = new RubySassCssProcessor();
      final Callable<Void> task = new Callable<Void>() {
        public Void call() {
          try {
            processor.process(null, new StringReader("$side: top;$radius: 10px;.rounded-#{$side} {border-#{$side}-radius: $radius;}"), new StringWriter());
          } catch (final Exception e) {
            throw new RuntimeException(e);
          }
          return null;
        }
      };
      WroTestUtils.runConcurrently(task);
    }

}
