/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.config;

/**
 *
 * @author Alex Objelean
 */
public class WroProperties {
  private boolean ignoreMissingResources = true;
  private boolean removeDuplicateResources = true;


  /**
   * @return the ignoreMissingResources
   */
  public boolean isIgnoreMissingResources() {
    return this.ignoreMissingResources;
  }


  /**
   * @param ignoreMissingResources the ignoreMissingResources to set
   */
  public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    this.ignoreMissingResources = ignoreMissingResources;
  }


  /**
   * @return the removeDuplicateResources
   */
  public boolean isRemoveDuplicateResources() {
    return this.removeDuplicateResources;
  }


  /**
   * @param removeDuplicateResources the removeDuplicateResources to set
   */
  public void setRemoveDuplicateResources(final boolean removeDuplicateResources) {
    this.removeDuplicateResources = removeDuplicateResources;
  }
}
