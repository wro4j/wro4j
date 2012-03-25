package ro.isdc.wro.extensions.http;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.http.support.AbstractProcessorsFilter;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * A filter which transforms a coffeeScript resource into javascript.
 * 
 * @author Alex Objelean
 * @since 1.4.5
 * @created 22 Mar 2012
 */
public class CoffeeScriptFilter
    extends AbstractProcessorsFilter {
  private List<ResourcePreProcessor> list = new ArrayList<ResourcePreProcessor>();

  public CoffeeScriptFilter() {
    list.add(new CoffeeScriptProcessor());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected List<ResourcePreProcessor> getProcessorsList() {
    return list;
  }
  
}
