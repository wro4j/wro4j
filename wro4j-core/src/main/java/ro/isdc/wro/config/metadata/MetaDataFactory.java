package ro.isdc.wro.config.metadata;

import java.util.Map;

import ro.isdc.wro.util.ObjectFactory;


/**
 * Factory responsible for creating/providing metadata. Metadata is a storage of custom set of runtime configurations.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public interface MetaDataFactory
    extends ObjectFactory<Map<String, Object>> {
}
