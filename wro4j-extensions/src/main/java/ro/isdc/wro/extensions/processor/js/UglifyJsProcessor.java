/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.algorithm.uglify.UglifyJs;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;


/**
 * Compress js using uglifyJs utility.
 *
 * @author Alex Objelean
 * @created 7 Nov 2010
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class UglifyJsProcessor extends BeautifyJsProcessor {
  /**
   * @return new instance of {@link UglifyJs} engine.
   */
  @Override
  protected UglifyJs newEngine() {
    return UglifyJs.uglifyJs();
  }
}
