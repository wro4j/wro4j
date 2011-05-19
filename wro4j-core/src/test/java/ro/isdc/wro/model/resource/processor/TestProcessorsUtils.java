/**
 * Copyright wro4j@2001
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

import ro.isdc.wro.model.resource.Resource;

/**
 * @author Alex Objelean
 */
public class TestProcessorsUtils {

  private static class PreProcessor1 implements ResourcePreProcessor {
    public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {}
  }

  @Test
  public void test() {
//    ProcessorsUtils.findPreProcessorByClass(processorClass, preProcessors)
  }
}
