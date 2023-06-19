package ro.isdc.wro.extensions.support.lint;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Contains fields for a single lint error item.  
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class LintItem implements Serializable {
  /**
   * The line (relative to 0) at which the lint was found
   */
  private int line;
  /**
   * The character (relative to 0) at which the lint was found
   */
  private int column;
  /**
   * The problem
   */
  private String reason;
  /**
   * The text line in which the problem occurred
   */
  private String evidence;
  /**
   * Type of error.
   */
  private String severity;

  /**
   * @return the line
   */
  public int getLine() {
    return this.line;
  }


  /**
   * @param line the line to set
   */
  public void setLine(final int line) {
    this.line = line;
  }

  /**
   * @return the reason
   */
  public String getReason() {
    return this.reason;
  }


  /**
   * @param reason the reason to set
   */
  public void setReason(final String reason) {
    this.reason = reason;
  }


  /**
   * @return the evidence
   */
  public String getEvidence() {
    return this.evidence;
  }


  /**
   * @param evidence the evidence to set
   */
  public void setEvidence(final String evidence) {
    this.evidence = evidence;
  }
  

  public final int getColumn() {
    return column;
  }


  public final void setColumn(final int column) {
    this.column = column;
  }


  public final String getSeverity() {
    return severity;
  }


  public final void setSeverity(final String type) {
    this.severity = type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
