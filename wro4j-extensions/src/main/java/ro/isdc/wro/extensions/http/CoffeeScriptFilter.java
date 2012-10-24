package ro.isdc.wro.extensions.http;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor;
import ro.isdc.wro.http.support.AbstractProcessorsFilter;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;

/**
 * A filter which transforms a coffeeScript resource into javascript.
 *
 * @author Alex Objelean
 * @since 1.4.5
 * @created 22 Mar 2012
 */
public class CoffeeScriptFilter
    extends AbstractProcessorsFilter {
  private final List<ResourceProcessor> list = new ArrayList<ResourceProcessor>();

  public CoffeeScriptFilter() {
    list.add(new RhinoCoffeeScriptProcessor());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<ResourceProcessor> getProcessorsList() {
    return list;
  }

}
