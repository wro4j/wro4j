/*
 * Copyright (C) 2012.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * A processor to support the bourbon (http://thoughtbot.com/bourbon/) mixins library for sass.
 * Using this processor automatically provides sass support, so there is no need to use both this one and
 * the {@link RubySassCssProcessor}.
 *
 * @author Simon van der Sluis
 * @since 1.4.7
 */
public class BourbonCssProcessor extends RubySassCssProcessor implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * The processor alias.
   */
  public static final String ALIAS = "bourbonCss";
  private static final String BOURBON_GEM_REQUIRE = "bourbon";

  /**
   * A getter used for lazy loading, overrides RubySassEngine#getEngine() and ensure the
   * bourbon gem is imported (required).
   */
  @Override
  protected RubySassEngine newEngine() {
    final RubySassEngine engine = super.newEngine();
    engine.addRequire(BOURBON_GEM_REQUIRE);
    return engine;
  }
}
