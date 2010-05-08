/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.algorithm;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;

/**
 * @author Alex Objelean
 * @created 09 may, 2010
 */
public class TestDataUriGenerator extends AbstractWroTest {

  @Test
  public void test()
      throws Exception {
    final Writer writer = new OutputStreamWriter(System.out);
    new DataUriGenerator().generateDataURI(getInputStream("classpath:ro/isdc/wro/processor/dataUri/btn_icons.png"),
        writer, "btn_icons.png");
    writer.close();
  }

}
