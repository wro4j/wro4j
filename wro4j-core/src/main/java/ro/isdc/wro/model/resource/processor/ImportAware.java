package ro.isdc.wro.model.resource.processor;

/**
 * Processors implementing this interface will be applied (by returning true for {@link ImportAware#isImportAware()}
 * method) on imported resources during pre-processing execution. A processor is
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public interface ImportAware {
  /**
   * @return flag indicating if the implementor of this interface should process also imported (referred by @import directive)
   *         resources.
   */
  boolean isImportAware();
}
