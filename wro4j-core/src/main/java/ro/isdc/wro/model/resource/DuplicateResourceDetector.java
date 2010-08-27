/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Used to detect duplicated resources. The implementation holds a detector context associated with each thread. This is necessary because the detection should be thread-safe.
 *
 * @author Alex Objelean
 */
public class DuplicateResourceDetector {
  private static final ThreadLocal<DetectorContext> DETECTOR_CONTEXT = new ThreadLocal<DuplicateResourceDetector.DetectorContext>();
  private class DetectorContext {
    private final Collection<String> resourceUris = new ArrayList<String>();
  }

  private DetectorContext getSafeContext() {
    DetectorContext context = DETECTOR_CONTEXT.get();
    if (context == null) {
      context = new DetectorContext();
      DETECTOR_CONTEXT.set(context);
    }
    return DETECTOR_CONTEXT.get();
  }

  public DuplicateResourceDetector() {
  }


  public void addResourceUri(final String resourceUri) {
    getSafeContext().resourceUris.add(resourceUri);
  }

  /**
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
