package ro.isdc.wro.model.resource.processor.support;

import java.util.HashMap;
import java.util.Map;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
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
  public static final ResourcePreProcessor CUSTOM = new ProcessorDecorator(new CssMinProcessor());

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePreProcessor> providePreProcessors() {
    final Map<String, ResourcePreProcessor> map = new HashMap<String, ResourcePreProcessor>();
    map.put(ALIAS, CUSTOM);
    return map;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, ResourcePostProcessor> providePostProcessors() {
    return null;
  }


  /**
   * {@inheritDoc}
   */
  public int getOrder() {
    return Ordered.HIGHEST;
  }
}
