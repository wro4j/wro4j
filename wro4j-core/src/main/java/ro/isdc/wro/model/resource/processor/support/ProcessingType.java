package ro.isdc.wro.model.resource.processor.support;

/**
 * A type of processing to apply during preProcessor execution.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
public enum ProcessingType {
  /**
   * Applies all eligible processors.
   */
  ALL,
  /**
   * Applies only processors which are interested of being applied for imported resources (ex: using @import directive
   * for css). This is necessary to fix the problem of LessCss processor (or similar) when using as preProcessor.
   */
  IMPORT_ONLY
}