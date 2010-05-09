/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.algorithm;

import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;

/**
 * @author Alex Objelean
 * @created 09 may, 2010
 */
public class TestDataUriGenerator extends AbstractWroTest {
  private DataUriGenerator dataUriGenerator = new DataUriGenerator();
  @Test
  public void test()
    throws Exception {
    dataUriGenerator.generateDataURI(getInputStream("classpath:ro/isdc/wro/processor/dataUri/btn_icons.png"),
      "btn_icons.png");
  }

}
