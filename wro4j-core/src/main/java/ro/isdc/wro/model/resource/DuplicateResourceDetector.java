/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

import java.util.Collection;
import java.util.HashSet;


/**
 * Used to detect duplicated resources. The implementation holds a detector context associated with each thread. This is
 * necessary because the detection should be thread-safe. It is responsibility of the client to populate the resources
 * being processed during a single request cycle and to reset it after the processing is complete.
 *
 * @author Alex Objelean
 */
public class DuplicateResourceDetector {
  private static final ThreadLocal<DetectorContext> DETECTOR_CONTEXT = new ThreadLocal<DuplicateResourceDetector.DetectorContext>();
  private class DetectorContext {
    private final Collection<String> resourceUris = new HashSet<String>();
  }

  private DetectorContext getSafeContext() {
    DetectorContext context = DETECTOR_CONTEXT.get();
    if (context == null) {
      context = new DetectorContext();
      DETECTOR_CONTEXT.set(context);
    }
    return DETECTOR_CONTEXT.get();
  }


  public void addResourceUri(final String resourceUri) {
    getSafeContext().resourceUris.add(resourceUri);
  }

  /**
   * Checks if the resource uri is duplicate.
   * @param resourceUri
   * @return true if the resourceUri is
   */
  public boolean isDuplicateResourceUri(final String resourceUri) {
    if (resourceUri == null) {
      throw new IllegalArgumentException("ResourceUri cannot be null!");
    }
    return getSafeContext().resourceUris.contains(resourceUri);
  }

  /**
   * Reset the duplication search context.
   */
  public void reset() {
    DETECTOR_CONTEXT.remove();
  }
}
