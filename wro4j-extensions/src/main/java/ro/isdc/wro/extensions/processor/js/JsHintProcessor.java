/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import ro.isdc.wro.extensions.processor.support.linter.AbstractLinter;
import ro.isdc.wro.extensions.processor.support.linter.JsHint;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * <p>Processor which analyze the js code and warns you about any problems. The
 * processing result won't change no matter if the processed script contains
 * errors or not.</p>
 *
 * <p>The version of jshint used by this processor is a snapshot committed on the
 * following date: 2012-11-13 05:25:37.</p>
 *
 * @author Alex Objelean
 * @since 1.3.5
 */
@SupportedResourceType(ResourceType.JS)
public class JsHintProcessor extends AbstractLinterProcessor {
	public static final String ALIAS = "jsHint";

	@Override
	protected AbstractLinter newLinter() {
		return new JsHint();
	}
}