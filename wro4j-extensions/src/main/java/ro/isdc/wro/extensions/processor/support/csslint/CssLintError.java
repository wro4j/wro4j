/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.csslint;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Encapsulates an error reported by JsHint.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
@SuppressWarnings("serial")
public class CssLintError
  implements Serializable {
  /**
   * Type of error.
   */
  private String type;
  /**
   * Detailed error message.
   */
  private String message;
  /**
   * The rule with the problem.
   */
  private CssRule rule;
  private int line;
  private int col;
  private String evidence;


  /**
   * @return the type
   */
  public String getType() {
    return this.type;
  }


  /**
   * @param type the type to set
   */
  public void setType(final String type) {
    this.type = type;
  }


  /**
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }


  /**
   * @param message the message to set
   */
  public void setMessage(final String message) {
    this.message = message;
  }


  /**
   * @return the rule
   */
  public CssRule getRule() {
    return this.rule;
  }


  /**
   * @param rule the rule to set
   */
  public void setRule(final CssRule rule) {
    this.rule = rule;
  }


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
   * @return the col
   */
  public int getCol() {
    return this.col;
  }


  /**
   * @param col the col to set
   */
  public void setCol(final int col) {
    this.col = col;
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


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
