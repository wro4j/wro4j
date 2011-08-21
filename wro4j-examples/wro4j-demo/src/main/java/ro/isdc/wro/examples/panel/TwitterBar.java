/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.examples.panel;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class TwitterBar
    extends Panel {
  public TwitterBar(final String id) {
    super(id);
  }

//  private String interpolateScript()
//      throws Exception {
//    final Map<String, Object> map = buildMap();
//    final String content = getTemplate().asString(map);
//    final String script = MapVariableInterpolator.interpolate(content, map);
//    return script;
//  }

//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  protected TextTemplate getTemplate() {
//    return new PackagedTextTemplate(RestrictedPlaytechFacadeScript.class, SCRIPT_NAME);
//  }
}
