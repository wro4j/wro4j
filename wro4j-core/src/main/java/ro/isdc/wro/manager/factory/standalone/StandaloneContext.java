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
   * Token used to Separate multiple context folders.
   */
  private static final String TOKEN_SEPARATOR = ",\\s*";
  /**
   * Exact location of the wro file.
   */
  private File wroFile;
  private String contextFoldersAsCSV;
  private boolean minimize;
  /**
   * Inform the factory about the intention of ignoring the missing resources. If true - the missing resources will be
   * ignored. This value is string, because the null is also accepted (meaning that the default value will be used).
   */
  private String ignoreMissingResourcesAsString;

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

  /**
   * @return string representation of context folders. The value can be a single value or a comma separated list of
   *         folders. Use {@link #getContextFolders()} to get the array of folders.
   */
  public String getContextFoldersAsCSV() {
    return contextFoldersAsCSV;
  }

  /**
   * @param contextFoldersAsCSV a comma separated list of context folders.
   */
  public void setContextFoldersAsCSV(final String contextFoldersAsCSV) {
    this.contextFoldersAsCSV = contextFoldersAsCSV;
  }

  /**
   * @return an array of context folders after each folder is split from CSV. When no context folder is set, an empty
   *         array will be returned.
   */
  public String[] getContextFolders() {
    return contextFoldersAsCSV != null ? contextFoldersAsCSV.split(TOKEN_SEPARATOR) : new String[] {};
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
  public String getIgnoreMissingResourcesAsString() {
    return this.ignoreMissingResourcesAsString;
  }

  /**
   * @param ignoreMissingResourcesAsString the ignoreMissingResources to set
   */
  public void setIgnoreMissingResourcesAsString(final String ignoreMissingResourcesAsString) {
    this.ignoreMissingResourcesAsString = ignoreMissingResourcesAsString;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
