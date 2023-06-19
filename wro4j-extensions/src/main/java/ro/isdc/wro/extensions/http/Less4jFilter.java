package ro.isdc.wro.extensions.http;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.extensions.processor.css.Less4jProcessor;
import ro.isdc.wro.http.support.AbstractProcessorsFilter;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * A filter which transforms a less resource into css.
 *
 * @author Alex Objelean
 * @since 1.7.4
 */
public class Less4jFilter
    extends AbstractProcessorsFilter {
  private final List<ResourcePreProcessor> list = new ArrayList<ResourcePreProcessor>();

  public Less4jFilter() {
    list.add(new Less4jProcessor());
  }

  @Override
  protected List<ResourcePreProcessor> getProcessorsList() {
    return list;
  }

  @Override
  protected Resource createResource(final String requestUri) {
    return Resource.create(requestUri, ResourceType.CSS);
  }
}
