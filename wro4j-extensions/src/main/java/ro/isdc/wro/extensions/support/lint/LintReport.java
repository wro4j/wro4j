package ro.isdc.wro.extensions.support.lint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Encapsulates information about lint errors associated with a collection of resources. This class is generic, the type
 * {@code <T>} describe the type of lint errors.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public final class LintReport<T> {
  private final List<ResourceLintReport<T>> reports;

  public LintReport() {
    reports = new ArrayList<>();
  }

  /**
   * @return a readonly collection of resource
   */
  public List<ResourceLintReport<T>> getReports() {
    return Collections.unmodifiableList(reports);
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
