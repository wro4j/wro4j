package ro.isdc.wro.model.resource.processor;

/**
 * Implementors of this interface will be able to provide information about its support. A sample use case: a processor
 * which indicates that the implementation is supported on current environment (some processors may be supported on
 * Linux, but unsupported on other platforms), or if certain prerequisites are met.
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
public interface SupportAware {
  /**
   * @return true if the implementor of this interface is supported.
   */
  boolean isSupported();
}
