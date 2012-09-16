package ro.isdc.wro.extensions.processor.support.lint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;


/**
 * Encapsulates information about lint errors associated with a collection of resources. This class is generic, the type
 * <T> describe the type of lint errors.
 * 
 * @author Alex Objelean
 * @created 16 Sep 2012
 * @since 1.4.10
 */
public final class LintReport<T> {
  private List<ResourceLintReport<T>> reports;
  
  public LintReport() {
    reports = new ArrayList<ResourceLintReport<T>>() {};
  }
  
  /**
   * @return a readonly collection of resource
   */
  public List<ResourceLintReport<T>> getReports() {
    return Collections.unmodifiableList(reports);
  }
  
  public void setReports(final List<ResourceLintReport<T>> reports) {
    Validate.notNull(reports);
    this.reports = reports;
  }
  
  /**
   * Add a single lint report to underlying collection.
   * 
   * @param resourceLintReport
   *          {@link ResourceLintReport} to add.
   * @return reference to this {@link LintReport} object (fluent interface).
   */
  public LintReport<T> addReport(final ResourceLintReport<T> resourceLintReport) {
    Validate.notNull(resourceLintReport);
    reports.add(resourceLintReport);
    return this;
  }
}
