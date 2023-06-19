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
public class CssRule
  implements Serializable {
  /**
   * Rule id.
   */
  private String id;
  /**
   * Rule name.
   */
  private String name;
  /**
   * Detailed description of the rule.
   */
  private String desc;
  /**
   * Affected browsers.
   */
  private String browsers;


  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }


  /**
   * @param id the id to set
   */
  public void setId(final String id) {
    this.id = id;
  }


  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }


  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }


  /**
   * @return the description
   */
  public String getDesc() {
    return this.desc;
  }


  /**
   * @param description the description to set
   */
  public void setDesc(final String description) {
    this.desc = description;
  }


  /**
   * @return the browsers
   */
  public String getBrowsers() {
    return this.browsers;
  }


  /**
   * @param browsers the browsers to set
   */
  public void setBrowsers(final String browsers) {
    this.browsers = browsers;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
