package ro.isdc.wro.model.resource.processor;

/**
 * Implementors of this interface .
 *
 * @author Alex Objelean
 * @created 11 Sep 2012
 * @since 1.5.1
 */
public interface ImportAware {
  /**
   * @return true if the implementor of this interface should process also imported (referred by @import directive)
   *         resources..
   */
  boolean isImportAware();
}
