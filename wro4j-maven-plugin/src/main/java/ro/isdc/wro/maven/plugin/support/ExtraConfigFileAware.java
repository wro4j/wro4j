/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.maven.plugin.support;

import java.io.File;

/**
 * Allows implementor to access a file containing extra config file used by maven plugin.
 *
 * @author Alex Objelean
 */
public interface ExtraConfigFileAware {
  /**
   * The implementation should handle the null case also.
   *
   * @param extraConfigFile
   *          the file containing extra configurations.
   */
  void setExtraConfigFile(File extraConfigFile);
}
