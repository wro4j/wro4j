package ro.isdc.wro.examples.support.spi;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.examples.support.processor.CustomProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.ProcessorProvider;


/**
 * Sample provider
 *
 * @author Alex Objelean
 */
public class CustomProcessorProvider
    implements ProcessorProvider {
  @Override
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put(CustomProcessor.ALIAS, new CustomProcessor());
    return map;
  }

  @Override
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return new HashMap<String, ResourcePostProcessor>();
  }
}
