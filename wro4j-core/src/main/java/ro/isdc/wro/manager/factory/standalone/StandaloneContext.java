/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.manager.factory.standalone;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * A context for running wro4j in standalone mode. This context can be used by a build time solutions like maven.
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class StandaloneContext
  implements Serializable {
  /**
   * Exact location of the wro file.
   */
  private File wroFile;
  private String contextFolders;
  private boolean minimize;
  /**
   * Inform the factory about the intention of ignoring the missing resources. If true - the missing resources will be
   * ignored.
   */
  private boolean ignoreMissingResources;


  /**
   * @return the wroFile
   */
  public File getWroFile() {
    return wroFile;
  }


  /**
   * @param wroFile the wroFile to set
   */
  public void setWroFile(final File wroFile) {
    this.wroFile = wroFile;
  }


  public String getContextFolders() {
    return contextFolders;
  }


  public void setContextFolders(final String contextFolders) {
    this.contextFolders = contextFolders;
  }


  /**
   * @return the minimize
   */
  public boolean isMinimize() {
    return minimize;
  }


  /**
   * @param minimize the minimize to set
   */
  public void setMinimize(final boolean minimize) {
    this.minimize = minimize;
  }


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
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
