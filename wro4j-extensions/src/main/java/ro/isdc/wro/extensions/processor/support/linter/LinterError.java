/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Encapsulates an error reported by JsHint.
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
@SuppressWarnings("serial")
public class LinterError
  implements Serializable {
  /**
   * The line (relative to 0) at which the lint was found
   */
  private int line;
  /**
   * The character (relative to 0) at which the lint was found
   */
  private int character;
  /**
   * The problem
   */
  private String reason;
  /**
   * The text line in which the problem occurred
   */
  private String evidence;


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
   * @return the character
   */
  public int getCharacter() {
    return this.character;
  }


  /**
   * @param character the character to set
   */
  public void setCharacter(final int character) {
    this.character = character;
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


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
