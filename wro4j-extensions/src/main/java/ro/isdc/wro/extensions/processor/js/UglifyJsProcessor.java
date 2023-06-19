/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.uglify.UglifyJs;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * Compress js using uglifyJs utility.
 *
 * @author Alex Objelean
 * @since 1.3.1
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class UglifyJsProcessor extends BeautifyJsProcessor {
  public static final String ALIAS_UGLIFY = "uglifyJs";
  /**
   * @return new instance of {@link UglifyJs} engine.
   */
  @Override
  protected UglifyJs newEngine() {
    return UglifyJs.uglifyJs();
  }
}
