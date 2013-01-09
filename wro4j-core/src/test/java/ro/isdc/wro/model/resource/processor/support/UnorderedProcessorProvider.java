package ro.isdc.wro.model.resource.processor.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * A custom processor provider used to test that the processors provided by this provider have precedence over default
 * processors.
 *
 * @author Alex Objelean
 */
public class UnorderedProcessorProvider
    implements ProcessorProvider {
  public static ResourceProcessor CUSTOM = new ProcessorDecorator(new JSMinProcessor());
  public static ResourceProcessor CONFORM_COLORS = new ProcessorDecorator(new ConformColorsCssProcessor());

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceProcessor> providePreProcessors() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    map.put(OrderedProcessorProvider.ALIAS, CUSTOM);
    map.put(ConformColorsCssProcessor.ALIAS, CONFORM_COLORS);
    return map;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceProcessor> providePostProcessors() {
    return null;
  }
}
