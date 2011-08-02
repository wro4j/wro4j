/*
 * Copyright (C) 2011 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.maven.plugin.manager;

import java.io.File;

/**
 * Allows implementor to access a file containing extra properties used by maven plugin.
 *
 * @author Alex Objelean
 */
public interface ExtraPropertiesFileAware {
  /**
   * The implementation should handle the null case also.
   *
   * @param extraProperties
   *          the file containing extra properties.
   */
  void setExtraPropertiesFile(File extraProperties);
}
