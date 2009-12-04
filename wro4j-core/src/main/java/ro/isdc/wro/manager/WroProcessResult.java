/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.InputStream;

import ro.isdc.wro.resource.ResourceType;

/**
 * WroProcessResult. Contains the result of the manager processing.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 14, 2008
 */
public final class WroProcessResult {
  /**
   * The content type of the stream content.
   */
  private ResourceType resourceType;

  /**
   * The stream content. Can be merged js, css & or image stream from some
   * remote location.
   */
  private InputStream inputStream;

  /**
   * @return the resourceType
   */
  public ResourceType getResourceType() {
    return this.resourceType;
  }

  /**
   * @param resourceType the resourceType to set
   */
  public void setResourceType(final ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * @return the inputStream
   */
  public final InputStream getInputStream() {
    return inputStream;
  }

  /**
   * @param inputStream
   *          the inputStream to set
   */
  public final void setInputStream(final InputStream inputStream) {
    this.inputStream = inputStream;
  }
}
