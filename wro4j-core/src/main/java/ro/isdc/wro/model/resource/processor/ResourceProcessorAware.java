package ro.isdc.wro.model.resource.processor;

/**
 * Marker interface which extends all interfaces a processor can extend.
 *
 * @author Alex Objelean
 * @since 1.7.1
 */
public interface ResourceProcessorAware
    extends ResourcePreProcessor, ResourcePostProcessor, SupportedResourceTypeAware, MinimizeAware, SupportAware,
    ImportAware, Destroyable {

}
