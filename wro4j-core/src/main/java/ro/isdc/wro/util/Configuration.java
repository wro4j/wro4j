/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.util;

/**
 * Configuration. There are two types: development used for debugging (no
 * caching) & deployment used in production (with caching).
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 18, 2008
 */
public enum Configuration {
  DEPLOYMENT, DEVELOPMENT;

  /**
   * @return true if configuration is in DEVELOPMENT mode.
   */
  public boolean isDevelopment() {
    return this == DEVELOPMENT;
  }

  /**
   * @return true if configuration is in DEPLOYMENT mode.
   */
  public boolean isDeployment() {
    return this == DEPLOYMENT;
  }
}
