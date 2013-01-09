package ro.isdc.wro.model.resource.processor.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.util.Ordered;


/**
 * A custom processor provider used to test that the processors provided by this provider have precedence over default
 * processors.
 *
 * @author Alex Objelean
 */
public class OrderedProcessorProvider
    implements ProcessorProvider, Ordered {
  public static final String ALIAS = "custom";
  public static ResourceProcessor CUSTOM = new ProcessorDecorator(new CssMinProcessor());

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceProcessor> providePreProcessors() {
    final Map<String, ResourceProcessor> map = new HashMap<String, ResourceProcessor>();
    map.put(ALIAS, CUSTOM);
    return map;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourceProcessor> providePostProcessors() {
    return null;
  }


  /**
   * {@inheritDoc}
   */
  public int getOrder() {
    return Ordered.HIGHEST;
  }
}
