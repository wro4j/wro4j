import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.processor.algorithm.ResourceContentStripper;


/**
 * TestStripper.
 *
 * @author Alex Objelean
 * @created Created on Nov 25, 2008
 */
public class TestStripper {
  public static void main(final String[] args)
    throws IOException {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final InputStream is = classLoader.getResourceAsStream("js/jquery.js");
    final Writer writer = new StringWriter();
    IOUtils.copy(is, writer);
    //TODO: compare result with expected value
    ResourceContentStripper.stripCommentsAndWhitespace(writer.toString());
  }
}
