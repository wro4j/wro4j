/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;


/**
 * Mark processor implementing this interface that they are capable of providing supported {@link ResourceType}. This is
 * useful for Resource processor decorators, which should "inherit" the decorated resources
 * {@link SupportedResourceType}. This interface was created as a workaround, because you cannot set annotations at
 * runtime in java.
 *
 * @author Alex Objelean
 * @since 1.3.7
 */
public interface SupportedResourceTypeAware {
  /**
   * @return {@link SupportedResourceType} to be used by processor.
   */
  SupportedResourceType getSupportedResourceType();
}
