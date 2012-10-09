package ro.isdc.wro.examples.support.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.examples.support.processor.CustomProcessor;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;


/**
 * Sample provider
 *
 * @author Alex Objelean
 */
public class CustomProcessorProvider
    implements ProcessorProvider {
  public Map<String, ResourceProcessor> providePreProcessors() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    map.put(CustomProcessor.ALIAS, new CustomProcessor());
    return map;
  }

  public Map<String, ResourceProcessor> providePostProcessors() {
    return new HashMap<String, ResourceProcessor>();
  }
}
