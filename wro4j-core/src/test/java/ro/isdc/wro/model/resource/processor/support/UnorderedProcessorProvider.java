package ro.isdc.wro.model.resource.processor.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
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
  public static final ResourcePreProcessor CUSTOM = new ProcessorDecorator(new JSMinProcessor());
  public static final ResourcePreProcessor CONFORM_COLORS = new ProcessorDecorator(new ConformColorsCssProcessor());

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put(OrderedProcessorProvider.ALIAS, CUSTOM);
    map.put(ConformColorsCssProcessor.ALIAS, CONFORM_COLORS);
    return map;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return null;
  }
}
