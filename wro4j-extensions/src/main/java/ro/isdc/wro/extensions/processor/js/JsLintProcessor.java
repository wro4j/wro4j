/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.linter.AbstractLinter;
import ro.isdc.wro.extensions.processor.support.linter.JsLint;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;


/**
 * Processor which analyze the js code and warns you about any problems. The processing result won't change no matter if
 * the processed script contains errors or not.
 * This processor loads the jslint library from the webjar.
 *
 * @author Alex Objelean
 * @since 1.4.2
 */
@SupportedResourceType(ResourceType.JS)
public class JsLintProcessor
  extends AbstractLinterProcessor {
  public static final String ALIAS = "jsLint";
  /**
   * {@inheritDoc}
   */
  @Override
  protected AbstractLinter newLinter() {
    return new JsLint();
  }
}
