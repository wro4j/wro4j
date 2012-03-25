package ro.isdc.wro.extensions.http;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.http.support.AbstractProcessorsFilter;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * A filter which transforms a less resource into css.
 * 
 * @author Alex Objelean
 * @since 1.4.5
 * @created 18 Mar 2012
 */
public class LessCssFilter
    extends AbstractProcessorsFilter {
  private List<ResourcePreProcessor> list = new ArrayList<ResourcePreProcessor>();

  public LessCssFilter() {
    list.add(new LessCssProcessor());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected List<ResourcePreProcessor> getProcessorsList() {
    return list;
  }
  
}
